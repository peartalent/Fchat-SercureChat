const {connectDB, closeDB, connection} = require("../../db/conectionDB");
module.exports = {
    get: (callback) => {
        let sql = `SELECT * FROM view_message_user`;
        connection.query(sql, (err, rs, fields) => {
            if (!err) {
                callback(rs);
            } else console.log(err);
        });
    },
    // lọc theo tên của user để lấy ra message của họ
    getMessageUserByUserId: function (id1, id2, callback) {
        let sql = `SELECT * FROM view_message_user WHERE (sender_id = '${id1}' OR receiver_id ='${id1}') AND (sender_id = '${id2}' OR receiver_id ='${id2}') ORDER BY create_date ASC`
        connection.query(
            sql,
            (err, rs, fields) => {
                if (!err) {
                    console.log(sql)
                    callback(rs);
                } else console.log(err);
            }
        );
    },
    getGroupByGroupId: function (groupId, callback) {
        let sql = `SELECT * FROM view_message_group where group_id =${groupId};`
        connection.query(sql, (err, rs) => {
            if (!err) {
                callback(rs);
            } else console.log(err);
        });
    },
    getAllGroupByMemberId: function (id, callback) {
        // sai query, lam lại với group với group member
        let sql = `SELECT group_id FROM  group_member WHERE user_id ='${id}';`
        connection.query(sql, (err, rs) => {
            if (!err) {
                callback(rs);
            } else console.log(err);
        });
    },
    sendMessageToGroupById: function (message, callback) {
        let sql = `INSERT INTO message (create_date, type, status, content) 
            VALUES ('${message.create_date}', '${message.type}', 'default', '${message.content}');`
        connection.query(sql,
            (err, rs, fields) => {
                if (!err) {
                    console.log(rs)
                    let sql2 = `INSERT INTO \`fchat\`.\`group_message\` (\`group_receiver_id\`, \`msg_id\`, \`sender_id\`) VALUES (${message.group_id}, ${rs.insertId}, '${message.sender_id}')`
                    connection.query(sql2,
                        (err2, rs2) => {
                            if (!err2) {
                                callback({
                                    code: 2000,
                                    status: 1
                                });
                            } else {
                                callback({
                                    code: 2000,
                                    status: 0
                                });
                                console.log(err);
                            }
                        }
                    );
                } else {
                    {
                        callback({
                            code: 2000,
                            status: 0
                        });
                    }
                    console.log(err);
                }
            }
        );
    },
    sendMessageById: function (message, callback) {
        let sql = `INSERT INTO message (create_date, type, status, content) 
            VALUES ('${message.create_date}', '${message.type}', 'default', '${message.content}');`
        connection.query(sql,
            (err, rs, fields) => {
                if (!err) {
                    console.log(rs)
                    let sql2 = `INSERT INTO user_message (receiver_id, msg_id, sender_id) 
                        VALUES ('${message.sender_id}', ${rs.insertId}, '${message.receiver_id}')`
                    connection.query(sql2,
                        (err2, rs2) => {
                            if (!err2) {
                                callback({
                                    code: 2000,
                                    status: 1
                                });
                            } else {
                                callback({
                                    code: 2000,
                                    status: 0
                                });
                                console.log(err);
                            }
                        }
                    );
                } else {
                    {
                        callback({
                            code: 2000,
                            status: 0
                        });
                    }
                    console.log(err);
                }
            }
        );
    },
    getLastTimeMessageByFriend: (id1, id2, callback) => {
        let sql = `SELECT * FROM( SELECT MAX(msg_date) max_date FROM (SELECT v.create_date msg_date
    FROM user_friend uf,view_message_user v 
    WHERE ( uf.user1_id = v.sender_id OR uf.user1_id = v.receiver_id ) AND ( uf.user2_id = v.sender_id OR uf.user2_id = v.receiver_id ) 
    and (uf.user1_id = '${id2}' or uf.user1_id ='${id1}') and (uf.user2_id = '${id2}' or uf.user2_id ='${id1}')) 
    as ff) AS date,view_message_user  WHERE create_date = date.max_date`
        connection.query(sql,
            (err, rs, fields) => {
                if (!err) {
                    console.log(sql)
                    callback(rs);
                } else console.log(err);
            }
        );
    },
    updateStatusMessage: (msgId,callback)=>{
        let sql =`UPDATE message SET \`status\` = 'seen' WHERE \`msg_id\` = ${msgId}`
        connection.query(sql,
            (err, rs, fields) => {
                if (!err) {
                    console.log(sql)
                    callback({
                        code:200,
                        status:1
                    });
                } else {
                    console.log(err);
                    callback({
                        code:200,
                        status:0
                    });
                }
            }
        );
    },
    deleteMessage: (msgId,callback)=>{
        let sql =`UPDATE \`fchat\`.\`message\` SET \`type\` = 'other', \`content\` = 'delete' WHERE \`msg_id\` = ${msgId}`
        connection.query(sql,
            (err, rs, fields) => {
                if (!err) {
                    console.log(sql)
                    callback({
                        code:200,
                        status:1
                    });
                } else {
                    console.log(err);
                    callback({
                        code:200,
                        status:0
                    });
                }
            }
        );
    },
    deleteAllMessage:(userId1,userId2,callback)=>{
        let sql =`DELETE FROM \`user_message\`  WHERE  (receiver_id = '${userId1}' AND sender_id = '${userId2}') or (receiver_id = '${userId2}' AND sender_id = '${userId1}')`
        connection.query(sql,
            (err, rs, fields) => {
                if (!err) {
                    console.log(sql)
                    callback({
                        code:1,
                        status:1
                    });
                } else {
                    console.log(err);
                    callback({
                        code:12,
                        status:0
                    });
                }
            }
        );
    }
};
