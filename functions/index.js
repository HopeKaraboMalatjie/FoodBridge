const functions = require("firebase-functions");
const admin = require("firebase-admin");
const express = require("express");
const cors = require("cors"); 

admin.initializeApp();
const db = admin.firestore();
const app = express();
app.use(cors({ origin: true }));
app.use(express.json()); 

// GET /listings
app.get("/listings", async (req, res) => {
try {
const snapshot = await db.collection("listings").get();
const listings = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
return res.status(200).json(listings);
} catch (error) {
return res.status(500).send(error.message);
}
}); 

// POST /listings
app.post("/listings", async (req, res) => {
try {
const newListing = req.body;
const docRef = await db.collection("listings").add(newListing);
return res.status(201).json({ id: docRef.id, ...newListing });
} catch (error) {
return res.status(500).send(error.message);
}
}); 

// POST /listings/{id}/reserve
app.post("/listings/:id/reserve", async (req, res) => {
try {
const listingId = req.params.id;
const { userId } = req.body; 

await db.collection("listings").doc(listingId).update({
status: "reserved",
reservedBy: userId
});

return res.status(200).json({ message: "Reservation successful" });

} catch (error) {
return res.status(500).send(error.message);
}
}); 

// GET /users/{id}/impact
app.get("/users/:id/impact", async (req, res) => {
try {
const userId = req.params.id;
const userDoc = await db.collection("users").doc(userId).get();
if (!userDoc.exists) {
return res.status(404).send("User not found");
}
return res.status(200).json(userDoc.data().impact || { mealsSaved: 0, co2Reduced: 0 });
} catch (error) {
return res.status(500).send(error.message);
}
}); 

exports.api = functions.https.onRequest(app);