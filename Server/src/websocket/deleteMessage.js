const msgController = require('../controller/messageController')
const userController = require('../controller/userController')
const notificationController = require('../controller/notificationController')
const typeData = require("../../config/type_data");
const typeNotification = require("../../config/type_notification");
const notification = require("../util/notification")
const {KEY_SECRET} = require("../../config/key");
const {DELETE_CHAT} = require("../../config/notificationConfig");
const jwt = require("jsonwebtoken");
let deleteMessageToClient = (webSocketServer) => {
    webSocketServer.on("connection", function connection(socket) {
        socket.on("message", function incoming(data) {
            try {
                var jsonData = JSON.parse(data);
                console.log("type ---", jsonData)
                if (jsonData.type_data === typeData.USER_LOGIN) {
                    let user = jwt.verify(jsonData.token, KEY_SECRET);
                    console.log("user", user)
                    socket.user = user;
                }

                function socketIsConnected(id) {
                    for (const client of webSocketServer.clients) {
                        console.log("user_id", client.user.user_id)
                        if (client.hasOwnProperty('user') && client.readyState === client.OPEN
                            && client.user.user_id === id) {
                            return client;
                        }
                    }
                    return null;
                }

                if (jsonData.type_data === "delete") {
                    console.log("Co message")
                    console.log("message", jsonData)
                    if (jsonData.hasOwnProperty("token")) {
                        let user = jwt.verify(jsonData.token, KEY_SECRET);
                        socket.user = user;
                        if (jsonData.type_chat === "user") {
                            msgController.deleteMessage(jsonData.msg_id, (rs) => {
                                if (rs.status===1){
                                    webSocketServer.clients.forEach(function each(client) {
                                        if (client.hasOwnProperty('user') && client.readyState === client.OPEN
                                            && client.user.user_id === jsonData.receiver_id) {
                                            client.send(rs.toString())
                                        }
                                    });
                                    socket.send(rs.toString())
                                }
                            })
                            4
                        } else if (jsonData.type_chat === "group") {
                            console.log("message", jsonData)

                            msgController.deleteMessage(jsonData.msg_id, (rs) => {
                                if (rs.status === 1) {
                                    socket.send(rs.toString())
                                    msgController.getMemberOfGroupById(parseInt(jsonData.receiver_id), (members) => {
                                        members.forEach(member => {
                                            if (member.user_id != user.user_id) {
                                                if (socketIsConnected(member.user_id) != null) {
                                                    webSocketServer.clients.forEach(function each(client) {
                                                        if (client.hasOwnProperty('user') && client.readyState === client.OPEN
                                                            && client.user.user_id === member.user_id) {
                                                            client.send(message.content)
                                                            isSend = true
                                                        }
                                                    });
                                                   // socketIsConnected(member.user_id).send(rs.toString())
                                                }
                                            }
                                        })
                                    })
                                }
                            })

                        }
                    }
                }

                if (jsonData.type_data === "deleteAll") {
                    console.log("Co message")
                    console.log("message", jsonData)
                    if (jsonData.hasOwnProperty("token")) {
                        let user = jwt.verify(jsonData.token, KEY_SECRET);
                        socket.user = user;
                        if (jsonData.type_chat === "user") {
                            msgController.deleteAllMessage(jsonData.receiver_id,user.user_id, (rs) => {
                                if (rs.status===1){
                                    webSocketServer.clients.forEach(function each(client) {
                                        if (client.hasOwnProperty('user') && client.readyState === client.OPEN
                                            && client.user.user_id === jsonData.receiver_id) {
                                            client.send(rs.toString())
                                        }
                                    });
                                    socket.send(rs.toString())
                                    let notification = {
                                        type: DELETE_CHAT,
                                        content: "",
                                        userId: jsonData.receiver_id,
                                        senderId: user.user_id,
                                        createDate: require('moment')().format('YYYY-MM-DD HH:mm:ss')
                                    }
                                    notificationController.create(notification)
                                }
                            })
                        }
                    }
                }

            } catch (e) {
                console.log("bug", e); // error in the above string (in this case, yes)!
            }
        });
    });
}
module.exports = {
    deleteMessageToClient: deleteMessageToClient
}