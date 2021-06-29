const model = require("../model/notification");
const {KEY_SECRET} = require("../../config/key");
const jwt = require('jsonwebtoken');
const modelGroup = require("../model/group");
const fs = require('fs')

module.exports = {
    create: (req, res) => {
        let user = jwt.verify(req.params.token, KEY_SECRET);
        let notification = {
            type: req.body.type,
            content: req.body.content,
            userId: req.body.sender_id,
            senderId: user.user_id,
            createDate: require('moment')().format('YYYY-MM-DD HH:mm:ss')
        }
        console.log("xxxxxxxx notification", notification)
        model.createNotification(notification, (rs) => {
            res.json(rs)
        })
    },
    create: (notification) => {
        model.createNotification(notification, (rs) => {
        })
    },
    getNotificationsById: (req, res) => {
        let user = jwt.verify(req.params.token, KEY_SECRET);
        model.getById(user.user_id, async (rs) => {
            var notifications = []
            for (const row of rs) {
                let u = {
                    user_id: row.user_id,
                    avatar: row.avatar,
                    fullname: row.fullname,
                    sex: row.sex
                }
                if (row.type === "1" || row.type === "3" || row.type === "7") {
                    await new Promise(resolve => {
                        modelGroup.getById(Number(row.content), (group) => {
                            let notification = {
                                type: row.type,
                                content: row.content,
                                sender: u,
                                group: group[0],
                                create_date: row.create_date,
                                notification_id: row.notification_id,
                                status: row.status
                            }
                            notifications.push(notification)
                            resolve(true);
                        })
                    })

                } else {
                    let notification = {
                        type: row.type,
                        content: row.content,
                        sender: u,
                        create_date: row.create_date,
                        notification_id: row.notification_id,
                        status: row.status
                    }
                    notifications.push(notification)
                }

            }
            console.log("notifications ", notifications)
            res.json(notifications)
        })
    }
    ,
    updateNotificationsById: (req, res) => {
        let user = jwt.verify(req.params.token, KEY_SECRET);
        let id = req.params.id
        model.updateStatusNotification(id, (rs) => {
            console.log("update notification", rs)
            res.json(rs)
        })
    },
    updateAllNotificationsById: (req, res) => {
        let user = jwt.verify(req.params.token, KEY_SECRET);
        model.updateAllStatusNotification(user.user_id, (rs) => {
            console.log("update notification", rs)
            res.json(rs)
        })
    },
    deleteNotificationsById: (req, res) => {
        let user = jwt.verify(req.params.token, KEY_SECRET);
        let id = req.params.id
        model.deleteNotification(id, (rs) => {
            console.log("delete notification", rs)
            res.json(rs)
        })
    }
    ,
    deleteAllNotificationsById: (req, res) => {
        let user = jwt.verify(req.params.token, KEY_SECRET);
        model.deleteAllNotification(user.user_id, (rs) => {
            console.log("delete notification", rs)
            res.json(rs)
        })
    }
}
