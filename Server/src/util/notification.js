const admin = require("firebase-admin");
const serviceAccount = require("../../servicesAccountKey.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
});
// var payload = {
//     notification: {
//         title: "This is a Notification",
//         body: "This is the body of the notification message."

//     }
// };

var options = {
    priority: "high",
    timeToLive: 60 * 60 * 24
};

function sendOneDevice(registrationToken, payload) {
    admin.messaging().sendToDevice(registrationToken, payload, options)
        .then(function (response) {
            console.log("Successfully sent message:", response);
        })
        .catch(function (error) {
            console.log("Error sending message:", error);
        });
}


var topicName = 'industry-tech'

var topicName = 'industry-tech'

var message = {
    notification: {
        title: 'Sparky says hello!'
    },
    android: {
        notification: {
            image: 'https://foo.bar.pizza-monster.png'
        }
    },
    apns: {
        payload: {
            aps: {
                'mutable-content': 1
            }
        },
        fcm_options: {
            image: 'https://foo.bar.pizza-monster.png'
        }
    },
    webpush: {
        headers: {
            image: 'https://foo.bar.pizza-monster.png'
        }
    },
    topic: topicName,
};


function send(){
    admin.messaging().send(message)
        .then((response) => {
            // Response is a message ID string.
            console.log('Successfully sent message:', response);
        })
        .catch((error) => {
            console.log('Error sending message:', error);
        });
}


module.exports = {
    sendOneDevice: sendOneDevice,
    send:send
}