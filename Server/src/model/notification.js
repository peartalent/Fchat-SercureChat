const {connection} = require("../../db/conectionDB");
const userModel = require("./user")
const uNotice = require("../util/notification");
const typeNotification = require("../../config/type_notification");
module.exports = {
    getById: (id, callback) => {
        let sql = `SELECT \`user\`.user_id,\`user\`.sex ,\`user\`.avartar avatar, \`user\`.fullname, notification.notification_id,
notification.content,notification.create_date,notification.\`status\`,notification.type
 FROM \`user\`, notification WHERE \`user\`.user_id = notification.sender_id and notification.user_id= '${id}'`
        connection.query(
            sql, (err, rs) => {
                if (!err) {
                    console.log(rs)
                    callback(rs);
                } else console.log(err);
            });
    },
    createNotification: (noti, callback) => {
        let sql = `INSERT INTO \`fchat\`.\`notification\` (\`type\`, \`content\`, \`user_id\`, \`sender_id\`, \`create_date\`) VALUES ('${noti.type}', '${noti.content}', '${noti.userId}', '${noti.senderId}', '${noti.createDate}')`
        connection.query(sql, (err, rs) => {
            if (!err) {
                console.log(rs)
                userModel.getUserClientById(noti.userId, (u) => {
                    var payload = {
                        notification: {
                            title: "Thông báo",
                            body: "Bạn đã nhận được một thông báo"

                        }
                    };
                    uNotice.sendOneDevice(u.token_client, payload)
                })
                //
                callback({
                    code: 200,
                    status: 1
                })
            } else {
                console.log(err, sql)
                callback({
                    code: 200,
                    status: 0
                })
            }
        })
    },
    updateStatusNotification: (id, callback) => {
        let sql = `UPDATE \`fchat\`.\`notification\` SET status = 1 WHERE notification_id = ${id}`
        connection.query(sql, (err, rs) => {
            if (!err) {
                console.log(sql)
                callback({
                    code: 2,
                    status: 1
                })
            } else {
                console.log(err)
                callback({
                    code: 2,
                    status: 0
                })
            }
        })
    },

    updateAllStatusNotification: (id, callback) => {
        let sql = `UPDATE \`fchat\`.\`notification\` SET status = 1 WHERE user_id = '${id}'`
        connection.query(sql, (err, rs) => {
            if (!err) {
                console.log(sql)
                callback({
                    code: 3,
                    status: 1
                })
            } else {
                console.log(err)
                callback({
                    code: 3,
                    status: 0
                })
            }
        })
    },
    deleteNotification: (id, callback) => {
        let sql = `DELETE FROM \`fchat\`.\`notification\` WHERE \`notification_id\` = ${id}`
        connection.query(sql, (err, rs) => {
            if (!err) {
                console.log(rs)
                callback({
                    code: 3,
                    status: 1
                })
            } else {
                console.log(err)
                callback({
                    code: 3,
                    status: 0
                })
            }
        })
    }
    ,
    deleteNotificationByContentGroup: (groupId, callback) => {
        let sql = `DELETE FROM \`fchat\`.\`notification\` WHERE \`content\` = ${groupId}`
        connection.query(sql, (err, rs) => {
            if (!err) {
                console.log(rs)
                callback({
                    code: 3,
                    status: 1
                })
            } else {
                console.log(err)
                callback({
                    code: 3,
                    status: 0
                })
            }
        })
    }
    ,
    deleteNotificationByUserIdGroup: (userId, groupId) => {
        let sql = `DELETE FROM \`fchat\`.\`notification\` WHERE \`content\` = ${groupId} and user_id ='${userId}' `
        connection.query(sql, (err, rs) => {
            if (!err) {
                console.log(rs)
            } else {
                console.log(err)
            }
        })
    }
    ,
    deleteAllNotification: (id, callback) => {
        let sql = `DELETE FROM \`fchat\`.\`notification\` WHERE \`user_id\` = '${id}'`
        connection.query(sql, (err, rs) => {
            if (!err) {
                console.log(rs)
                callback({
                    code: 4,
                    status: 1
                })
            } else {
                console.log(err)
                callback({
                    code: 4,
                    status: 0
                })
            }
        })
    }
};