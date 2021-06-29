const controller = require("../model/friend");
const modelMessage = require("../model/message");
const controllerNotification = require("./notificationController");
const {KEY_SECRET} = require("../../config/key");
const {CANCEL_FRIEND,SEND_TO_FOLLOW,} = require("../../config/notificationConfig");
const jwt = require('jsonwebtoken');
module.exports = {
    getFriends: function (req, res) {
        controller.get((rs) => {
            res.json(rs);
        });
    },
    unFriendById: function (req, res) {
        let token = req.params.token
        let id2 = req.params.id2
        let user = jwt.verify(token, KEY_SECRET);
        controller.unfriendById(user.user_id,id2,(rs) => {
            console.log("un friend")
            modelMessage.deleteAllMessage(user.user_id,id2,()=>{})
            let notification = {
                type: CANCEL_FRIEND,
                content: "",
                userId: id2,
                senderId: user.user_id,
                createDate: require('moment')().format('YYYY-MM-DD HH:mm:ss')
            }
            controllerNotification.create(notification)
            res.json(rs);
        });
    },
    getFriendsByUserId: function (req, res) {
        let token = req.params.token
        let user = jwt.verify(token, KEY_SECRET);
        let id = user.user_id
        controller.getById(id, (rs) => {
            var friends = []
            rs.forEach(friend => {
                if (friend.user1_id === id) {
                    let u = {
                        user_id: friend.user2_id,
                        fullname: friend.name2,
                        avatar: friend.avatar2,
                        status: friend.status2,
                        last_online: friend.last_online2,
                        create_date: friend.create_date,
                        public_key: friend.public_key2,
                        sex : friend.sex2
                    }
                    friends.push(u)
                } else if (friend.user2_id === id) {
                    let u = {
                        user_id: friend.user1_id,
                        fullname: friend.name1,
                        avatar: friend.avatar1,
                        status: friend.status1,
                        last_online: friend.last_online1,
                        create_date: friend.create_date,
                        public_key: friend.public_key1,
                        sex : friend.sex1
                    }
                    friends.push(u)
                }
            })
            res.json(friends);
        });
    },

    getFriendsById: function (id) {
        controller.getById(id, (rs) => {
            var friends = []
            rs.forEach(friend => {
                if (friend.user1_id === id) {
                    let u = {
                        user_id: friend.user2_id,
                        fullname: friend.name2,
                        avatar: friend.avatar2,
                        status: friend.status2,
                        last_online: friend.last_online2,
                        create_date: friend.create_date,
                        public_key: friend.public_key2
                    }
                    friends.push(u)
                } else if (friend.user2_id === id) {
                    let u = {
                        user_id: friend.user1_id,
                        fullname: friend.name1,
                        avatar: friend.avatar1,
                        status: friend.status1,
                        last_online: friend.last_online1,
                        create_date: friend.create_date,
                        public_key: friend.public_key1
                    }
                    friends.push(u)
                }
            })
            console.log("xxx", friends)
            return friends;

        })
    },
    search: function (req, res) {
        let token = req.params.token
        let user = jwt.verify(token, KEY_SECRET);
        let name = req.params.name
        controller.searchByName(name, user.user_id,(rs) => {
            console.log("rs search",rs)
            res.json(rs);
        });
    },


};
