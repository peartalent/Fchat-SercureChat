const msgController = require('../../controller/messageController')
const userController = require('../../controller/userController')
const typeData = require("../../../config/type_data");
const typeNotification = require("../../../config/type_notification");
const notification = require("../../util/notification")
const {KEY_SECRET} = require("../../../config/key");
const notificationController = require('../../controller/notificationController')
const {SEND_KEY,SEND_KEY_GROUP} = require("../../../config/notificationConfig");
const jwt = require("jsonwebtoken");
let chatTextOneClient = (webSocketServer) => {
    webSocketServer.on("connection", function connection(socket) {
        console.log("connect text")
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
                if (jsonData.type_data === "create_key_secret") {
                    console.log("keyyyyyyyy",jsonData)
                    if (jsonData.hasOwnProperty("token")) {
                        let user = jwt.verify(jsonData.token, KEY_SECRET);
                        socket.user = user;
                        if (jsonData.type_chat === "user") {
                            console.log("type ---", jsonData)
                            userController.getUserClientById(jsonData.receiver_id, (rs) => {
                                let tt = typeNotification.TITLE_MESSAGE
                                let noti = {
                                    type: SEND_KEY,
                                    content: "",
                                    userId: jsonData.receiver_id,
                                    senderId: user.user_id,
                                    createDate: require('moment')().format('YYYY-MM-DD HH:mm:ss')
                                }
                                if (jsonData.group_id){
                                    tt = "group"+jsonData.group_id
                                    noti.content = jsonData.group_id+""
                                    noti.type = SEND_KEY_GROUP
                                }

                                notificationController.create(noti)
                                notification.sendOneDevice(rs.token_client,
                                    typeNotification.noticeKey(tt, jsonData.type_data, jsonData.content, user.user_id, rs.phone))
                                console.log("notive",typeNotification.noticeKey(tt, jsonData.type_data, jsonData.content, user.user_id, rs.phone))
                            })
                        }
                    }
                }
                else if (jsonData.type_data === typeData.USER_SEND_MESSAGE) {
                    console.log("Co message")
                    if (jsonData.hasOwnProperty("token")) {
                        let user = jwt.verify(jsonData.token, KEY_SECRET);
                        socket.user = user;
                        var message = JSON.parse(jsonData.message);
                        if (jsonData.type_chat === "user") {
                            if (message.type === typeData.MESSAGE_TYPE_OFF_TEXT || message.type === typeData.MESSAGE_TYPE_OFF_OTHER) {
                                var isSend = false
                                webSocketServer.clients.forEach(function each(client) {
                                    if (client.hasOwnProperty('user') && client.readyState === client.OPEN
                                        && client.user.user_id === message.receiver_id) {
                                        console.log("send 1 :" + client.user.user_id, client.readyState, client.OPEN, message.content)
                                        client.send(message.content)
                                        isSend = true
                                    }
                                });
                                if (isSend === false) {
                                    userController.getUserClientById(message.receiver_id, (rs) => {
                                        console.log(user)
                                        notification.sendOneDevice(rs.token_client,
                                            typeNotification.notice(typeNotification.TITLE_MESSAGE, typeNotification.NOTIFICATION_MESSAGE, rs.fullname, message.content, user.phone))
                                    })
                                }
                                isSend = false
                                message.sender_id = user.user_id
                                message.create_date = require('moment')().format('YYYY-MM-DD HH:mm:ss')
                                msgController.sendMessageById(message, (rs) => {
                                    if (rs.code === typeData.USER_SEND_MESSAGE) {
                                        socket.send(rs.toString())
                                    }
                                })
                            }
                        } else if (jsonData.type_chat === "group") {
                            console.log("message", jsonData)
                            if (message.type === typeData.MESSAGE_TYPE_OFF_TEXT || message.type === typeData.MESSAGE_TYPE_OFF_OTHER) {
                                message.sender_id = user.user_id
                                message.group_id = parseInt(message.receiver_id)
                                message.create_date = require('moment')().format('YYYY-MM-DD HH:mm:ss')
                                msgController.sendMessageGroup(message, (rs) => {
                                    socket.send(rs.toString())
                                    msgController.getMemberOfGroupById(parseInt(message.receiver_id), (members) => {
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
                                                  //  socketIsConnected(member.user_id).send(message.content)
                                                } else {
                                                    console.log("da gui notification")
                                                    userController.getUserClientById(member.user_id, (u) => {
                                                        notification.sendOneDevice(u.token_client,
                                                            typeNotification.notice(typeNotification.TITLE_MESSAGE, typeNotification.NOTIFICATION_MESSAGE, u.fullname, message.content, user.phone))
                                                    })
                                                }
                                            }
                                        })
                                    })
                                })
                            }
                        }
                    }
                }
            } catch (e) {
                console.log("bug", e); // error in the above string (in this case, yes)!
            }
        });
        socket.on('close', client => {
            console.log("client", "off")
        })
    });
}
module.exports = {
    chatTextOneClient: chatTextOneClient
}