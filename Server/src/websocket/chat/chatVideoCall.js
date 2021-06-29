const msgController = require('../../controller/messageController')
const userController = require('../../controller/userController')
const typeData = require("../../../config/type_data");
const typeNotification = require("../../../config/type_notification");
const notification = require("../../util/notification")
const {KEY_SECRET} = require("../../../config/key");
const jwt = require("jsonwebtoken");
const {v4: uuidv4} = require('uuid');
let chatVideoCallOneClient = (webSocketServer) => {
    webSocketServer.on("connection", function connection(socket) {
        socket.xxx = uuidv4().toString().replace(/-/g, "")
        console.log("ket noi call")
        socket.on("message", (data) => {
            try {
                var jsonData = JSON.parse(data);
                if (jsonData.type_data === typeData.USER_LOGIN) {
                    let user = jwt.verify(jsonData.token, KEY_SECRET);
                    console.log("user", user)
                    socket.user = user;
                } else if (jsonData.type_data === typeData.USER_CALL_VIDEO_ONE_CLIENT) {
                    if (jsonData.hasOwnProperty("token")) {
                        let user = jwt.verify(jsonData.token, KEY_SECRET);
                        console.log("goi cho", socket.user)

                        function socketIsConnected(id) {
                            for (const client of webSocketServer.clients) {
                                if (client.hasOwnProperty('user') && client.readyState === client.OPEN
                                    && client.user.user_id === id) {
                                    return client;
                                }
                            }
                            return null;
                        }

                        if (jsonData.type_chat === "user") {
                            var members = JSON.parse(jsonData.members);
                            console.log("users", members)
                            let clientConn = socketIsConnected(members[0].user_id)
                            if (clientConn) {
                                if (typeof jsonData.content === 'object') {
                                    clientConn.send(JSON.stringify(jsonData.content))
                                } else {
                                    clientConn.send(jsonData.content)
                                }
                            }
                            userController.getUserClientById(members[0].user_id, (u) => {
                                console.log("xxxxxxxxxxxx", u)
                                if (u !== undefined && u.token_client !== undefined) {
                                    notification.sendOneDevice(u.token_client,
                                        typeNotification.noticeCall(typeNotification.TITLE_CALL_RING, u.fullname, user.user_id, u.avartar,
                                            jsonData.content))
                                    console.log("notification ", typeNotification.noticeCall(typeNotification.TITLE_CALL_RING, u.fullname, user.user_id, u.avartar,
                                        jsonData.content))
                                }
                            })

                        } else if (jsonData.type_chat === "group") {
                            var members = JSON.parse(jsonData.members);
                            console.log("users", members)
                            members.forEach(m => {
                                if (m.user_id !== user.user_id) {
                                    let clientConn = socketIsConnected(m.user_id)
                                    if (clientConn) {
                                        if (typeof jsonData.content === 'object') {
                                            clientConn.send(JSON.stringify(jsonData.content))
                                        } else {
                                            clientConn.send(jsonData.content)
                                        }
                                    }
                                        userController.getUserClientById(m.user_id, (u) => {
                                            console.log("xxxxxxxxxxxx", u)
                                            if (u !== undefined && u.token_client !== undefined && u.token_client !== null) {
                                                notification.sendOneDevice(u.token_client,
                                                    typeNotification.noticeCall(typeNotification.TITLE_CALL_RING, u.fullname, user.user_id, u.avartar,
                                                        jsonData.content))
                                                console.log("notification ", typeNotification.noticeCall(typeNotification.TITLE_CALL_RING, u.fullname, user.user_id, u.avartar,
                                                    jsonData.content))
                                            }
                                        })
                                }

                            })

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
    chatVideoCallOneClient: chatVideoCallOneClient
}