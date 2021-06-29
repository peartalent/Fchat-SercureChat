const query = require("../model/follow");
const {KEY_SECRET} = require("../../config/key");
const jwt = require('jsonwebtoken');
const typeData = require("../../config/type_data");
const typeNotification = require("../../config/type_notification");
const noti = require("../util/notification")
const queryUser = require('./userController')
const {SEND_TO_FOLLOW,ACCEPT_FOLLOW} = require("../../config/notificationConfig");
const controllerNotification = require("./notificationController");
module.exports = {
    create: (req, res) => {
        let token = req.params.token
        let user = jwt.verify(token, KEY_SECRET);
        let id1 = user.user_id
        let data = req.body
        console.log(id1,data.id2)
        query.create(id1, data.id2, data.preface, (rs) => {
            if (rs) {
                console.log(rs)
                if (rs.status === 1) {
                    let notification = {
                        type: SEND_TO_FOLLOW,
                        content: "",
                        userId: data.id2,
                        senderId: user.user_id,
                        createDate: require('moment')().format('YYYY-MM-DD HH:mm:ss')
                    }
                    controllerNotification.create(notification)
                    queryUser.getUserClientById(data.id2,(u)=>{
                        noti.sendOneDevice(u.token_client,
                            typeNotification.notice(typeNotification.TITLE_FOLLOW,
                                typeNotification.NOTIFICATION_MESSAGE, u.fullname, typeNotification.MESSAGE_FOLLOW, user.phone))
                    })

                }
                res.json(rs)
            }
        })
    },
    getFollowUserByUserId: function (req, res) {
        let token = req.params.token
        let user = jwt.verify(token, KEY_SECRET);
        let id = user.user_id
        query.getFollowUserByUserId(id, (rs) => {
            var folows = []
            rs.forEach(follow => {
                let u = {
                    user_id: follow.user_id,
                    fullname: follow.fullname,
                    avatar: follow.avartar,
                    status: follow.status,
                    last_online: follow.last_online,
                    create_date: follow.create_date,
                    sex:follow.sex
                }
                folows.push(u)

            })
            console.log("follows",folows)
            res.json(folows);
        })
    },
    acceptFollow: function (req, res) {
        let token = req.params.token
        let id2 = req.body.id2
        let user = jwt.verify(token, KEY_SECRET);
        let id = user.user_id
        query.acceptFollow(id, id2, require('moment')().format('YYYY-MM-DD HH:mm:ss'), (rs) => {
            if (rs.status === 1) {
                queryUser.getUserClientById(id2,(u)=>{
                    let notification = {
                        type: ACCEPT_FOLLOW,
                        content: "",
                        userId: id2,
                        senderId: user.user_id,
                        createDate: require('moment')().format('YYYY-MM-DD HH:mm:ss')
                    }
                    controllerNotification.create(notification)
                    noti.sendOneDevice(u.token_client,
                        typeNotification.notice(typeNotification.TITLE_ACCEPT_FOLLOW,
                            typeNotification.NOTIFICATION_MESSAGE, u.fullname, typeNotification.MESSAGE_ACCEPT, user.phone))
                })
                res.json(rs);
            }

        })
    },
    // refus follow
    refuseFollow: function (req, res) {
        let token = req.params.token
        let id2 = req.params.id2
        let user = jwt.verify(token, KEY_SECRET);
        let id = user.user_id
        query.refuseFollow(id, id2, (rs) => {
            res.json(rs);
        })
    },
    // refus follow
    cancelFollow: function (req, res) {
        let token = req.params.token
        let id2 = req.params.id2
        let user = jwt.verify(token, KEY_SECRET);
        let id = user.user_id
        query.cancelFollow(id, id2, (rs) => {
            res.json(rs);
        })
    },
};