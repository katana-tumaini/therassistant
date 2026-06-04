const admin = require("firebase-admin");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");

admin.initializeApp();

exports.onNotificationCreated = onDocumentCreated(
    "notifications/{notificationId}",
    async (event) => {
        console.log("Notification created");
    }
);