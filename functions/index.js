const functions = require("firebase-functions");
const admin = require("firebase-admin");
const express = require("express");

admin.initializeApp(); // Initializes Firebase Admin for Firestore, etc.

const app = express();
app.use(express.json()); // Parse incoming JSON requests

// M-Pesa Callback Endpoint
app.post("/mpesa-callback", async (req, res) => {
    const callbackData = req.body;

    console.log("M-Pesa Callback Received:", JSON.stringify(callbackData, null, 2));

    // Handle callback data
    if (callbackData.Body && callbackData.Body.stkCallback) {
        const { ResultCode, ResultDesc, CallbackMetadata } = callbackData.Body.stkCallback;

        // Log the important callback data
        console.log("ResultCode:", ResultCode);
        console.log("ResultDesc:", ResultDesc);
        console.log("CallbackMetadata:", CallbackMetadata);

        // Save to Firestore (optional)
        try {
            await admin.firestore().collection("mpesaCallbacks").add({
                resultCode: ResultCode,
                resultDesc: ResultDesc,
                metadata: CallbackMetadata,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
            });
            console.log("Callback data saved to Firestore");
        } catch (error) {
            console.error("Error saving to Firestore:", error);
        }
    } else {
        console.error("Invalid callback data received:", callbackData);
    }

    // Respond to Safaricom with HTTP 200 (important)
    res.status(200).send("Callback received successfully");
});

// Export the Cloud Function
exports.api = functions.https.onRequest(app);
