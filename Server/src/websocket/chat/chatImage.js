const msgController = require('../../controller/messageController')
const userController = require('../../controller/userController')
const typeData = require("../../../config/type_data");
const typeNotification = require("../../../config/type_notification");
const notification = require("../../util/notification")
const {KEY_SECRET} = require("../../../config/key");
const jwt = require("jsonwebtoken");
const fs = require('fs')
let chatImageOneClient = (webSocketServer) => {
    webSocketServer.on("connection", function connection(socket) {
        console.log("connect image")
        socket.on("message", function incoming(data) {
            try {
                var jsonData = JSON.parse(data);
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

                if (jsonData.type_data === typeData.USER_SEND_MESSAGE) {
                    if (jsonData.hasOwnProperty("token")) {
                        let user = jwt.verify(jsonData.token, KEY_SECRET);
                        socket.user = user;
                        var message = JSON.parse(jsonData.message);
                        if (jsonData.type_chat === "user") {
                            console.log("message", jsonData)
                            if (message.type === typeData.MESSAGE_TYPE_OFF_IMAGE) {
                                console.log("Co message image")
                                var isSend = false
                                webSocketServer.clients.forEach(function each(client) {
                                    if (client.hasOwnProperty('user') && client.readyState === client.OPEN
                                        && client.user.user_id === message.receiver_id) {
                                        console.log("send image :" + client.user.user_id, client.readyState, client.OPEN)
                                        client.send(message.content)
                                        isSend = true
                                    }
                                });
                                if (isSend === false) {
                                    userController.getUserClientById(message.receiver_id, (rs) => {
                                        notification.sendOneDevice(rs.token_client,
                                            typeNotification.notice(typeNotification.TITLE_MESSAGE, typeNotification.NOTIFICATION_MESSAGE, rs.fullname, typeNotification.MESSAGE_IMAGE, user.user_id))
                                    })
                                }
                                message.sender_id = user.user_id
                                message.create_date = require('moment')().format('YYYY-MM-DD HH:mm:ss')
                                // message.content = getFilenameImage(socket.user.user_id).path_save
                                let nameFile = getFilenameImage(socket.user.user_id)
                                writeFileImage(nameFile.path, message.content)
                                message.content = nameFile.path_save

                                msgController.sendMessageById(message, (rs) => {
                                    if (rs.code === typeData.USER_SEND_MESSAGE) {
                                        socket.send(rs.toString())
                                        console.log("message image: ", 'đã gửi')
                                    }

                                })
                            }
                        } else if (jsonData.type_chat === "group") {
                            console.log("message", jsonData)
                            if (message.type === typeData.MESSAGE_TYPE_OFF_IMAGE) {
                                console.log("Co message image")
                                message.sender_id = user.user_id
                                message.group_id = parseInt(message.receiver_id)
                                message.create_date = require('moment')().format('YYYY-MM-DD HH:mm:ss')
                                // message.content = getFilenameImage(socket.user.user_id).path_save
                                let nameFile = getFilenameImage(socket.user.user_id)
                                writeFileImage(nameFile.path, message.content)
                                message.content = nameFile.path_save
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
                                                } else {
                                                    console.log("da gui notification")
                                                    userController.getUserClientById(member.user_id, (u) => {
                                                        console.log("uuuuuuu", u)
                                                        notification.sendOneDevice(u.token_client,
                                                            typeNotification.notice(typeNotification.TITLE_MESSAGE, typeNotification.NOTIFICATION_MESSAGE, u.fullname, typeNotification.MESSAGE_IMAGE, user.user_id))

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
    });
}

function writeFileImage(fileName, dataBase64) {
    fs.writeFile(fileName, dataBase64, 'base64', function (err) {
        console.log(err);
    });
}
function readFileBase64(path){
    return fs.readFileSync(path, 'base64')
}

function getFilenameImage(userId) {
    let now = new Date().getTime()
    let nameFile = `${userId}${now}`;
    return {
        path: `public/image/${nameFile}.png`,
        path_save: `/image/${nameFile}.png`
    }
}

module.exports = {
    chatImageOneClient: chatImageOneClient
}