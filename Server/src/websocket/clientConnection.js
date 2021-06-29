const {KEY_SECRET} = require("../../config/key");
const jwt = require("jsonwebtoken");
const typeData = require("../../config/type_data");
const userController = require('../controller/userController')

let loginClient = (webSocketServer) => {
    webSocketServer.on("connection", function connection(socket,req) {
        console.log("connect : ", req.connection.remoteAddress, ":",req.connection.remotePort)
        socket.on("message", function incoming(data) {
            try {
                var jsonData = JSON.parse(data);
                if (jsonData.type_data === typeData.USER_LOGIN) {
                    if (jsonData.hasOwnProperty("token")) {
                        let user = jwt.verify(jsonData.token, KEY_SECRET);
                        console.log("user",user)
                        socket.user = user;
                        if(jsonData.hasOwnProperty("token_client")){
                            userController.updateTokenById(jsonData.token,jsonData.token_client)
                        }
                        userController.updateStatus(user.user_id, "on")
                    }
                }
            } catch (e) {
                console.log(e); // error in the above string (in this case, yes)!
            }


        });

        socket.on('close', client => {
            if (socket.hasOwnProperty("user")){
                userController.updateStatus(socket.user.user_id, "off")
            }

        })
        socket.on('error', client => {
            userController.updateStatus(socket.user.user_id, "off")
        })

    });
}
module.exports.loginClient = loginClient