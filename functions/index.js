const admin = require("firebase-admin");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {logger} = require("firebase-functions");

admin.initializeApp();

exports.onNotificationCreated = onDocumentCreated(
    "notifications/{notificationId}",
    async (event) => {
        const notification = event.data ? event.data.data() : null;

        if (!notification || !notification.userId) {
            logger.warn("Notification missing userId, skipping push.");
            return;
        }

        const userDoc = await admin
            .firestore()
            .collection("users")
            .doc(notification.userId)
            .get();

        const fcmToken = userDoc.exists ? userDoc.get("fcmToken") : null;

        if (!fcmToken) {
            logger.warn(`No fcmToken for user ${notification.userId}, skipping push.`);
            return;
        }

        const message = {
            token: fcmToken,
            notification: {
                title: notification.title || "Therassistant",
                body: notification.message || "You have a new notification",
            },
            data: {
                notificationId: event.params.notificationId,
                type: notification.type || "",
                bookingId: notification.bookingId ? String(notification.bookingId) : "",
            },
            android: {
                priority: "high",
            },
        };

        try {
            await admin.messaging().send(message);
            logger.info(`Push sent to user ${notification.userId}`);
        } catch (error) {
            logger.error("Error sending push notification:", error);

            // Clean up invalid/expired tokens so we don't keep retrying them.
            if (
                error.code === "messaging/registration-token-not-registered" ||
                error.code === "messaging/invalid-registration-token"
            ) {
                await admin
                    .firestore()
                    .collection("users")
                    .doc(notification.userId)
                    .update({fcmToken: admin.firestore.FieldValue.delete()});
            }
        }
    }
);