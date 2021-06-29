const {connectDB, closeDB, connection} = require("../../db/conectionDB");
module.exports = {
    get: (callback) => {
        connection.query("select * from user_follow", (err, rs, fields) => {
            if (!err) {
                callback(rs);
            } else console.log(err);
        });
    },
    create: (id1, id2, preface, callback) => {
        let create_date = require('moment')().format('YYYY-MM-DD HH:mm:ss');
        let sql = `INSERT INTO user_follower (sender_id, receiver_id, create_date) VALUES ('${id1}', '${id2}', '${create_date}');`
        connection.query(sql, (err) => {
            if (!err) {
                callback({
                    code: 3000,
                    status: 1
                })
            } else {
                console.log(err)
                callback({
                    code: 3000,
                    status: 0
                })
            }
        })
    },
    // lọc theo tên của user để lấy ra message của họ
    getFollowUserByUserId: function (id, callback) {
        let sql =`SELECT u1.* ,f.create_date FROM user_follower f, user u1, user u2  WHERE u1.user_id = f.sender_id and u2.user_id = f.receiver_id and f.receiver_id = '${id}'`
        connection.query(sql,
            (err, rs, fields) => {
                if (!err) {
                    callback(rs);
                    console.log("follow",rs)
                } else console.log(err);
            }
        );
    },
    checkFollow:(senderId,recvId,callback)=>{
        let sql = `select * FROM user_follower WHERE sender_id ='${senderId}' and receiver_id ='${recvId}'`
        connection.query(sql,(err,rs)=>{
            if (!err){
                callback(rs)
            }else {
               console.log("checkfollow",err)
            }
        })
    }
    ,
    cancelFollow:(senderId,recvId,callback)=>{
        let sql = `Delete FROM user_follower WHERE sender_id ='${senderId}' and receiver_id ='${recvId}'`
        connection.query(sql,(err,rs)=>{
            if (!err){
                callback(rs)
            }else {
                console.log("checkfollow",err)
            }
        })
    }
    ,
    acceptFollow: function (id1, id2, create_date, callback) {
        connection.query(
            `INSERT INTO user_friend (user1_id, user2_id, create_date) VALUES ('${id1}', '${id2}', '${create_date}')`,
            (err) => {
                if (!err) {
                    connection.query(`DELETE FROM user_follower WHERE sender_id = '${id2}' AND receiver_id = '${id1}'`, (err2, rs2) => {
                        if (!err2) {
                            callback({
                                code: 3001,
                                status: 1,
                                result: rs2
                            });
                        }
                        console.log(err2);
                    })

                } else {
                    callback({
                        code: 3001,
                        status: 0
                    });
                    console.log(err);
                }
            }
        );
    },
    refuseFollow: function (id1, id2, callback) {
        connection.query(
            `DELETE FROM user_follower WHERE sender_id = '${id2}' AND receiver_id = '${id1}'`,
            (err, rs) => {
                if (!err) {
                    callback({
                        code: 3001,
                        status: 1,
                        result: rs
                    });

                } else {
                    console.log(err);
                    callback({
                        code: 3001,
                        status: 0
                    });
                }
            }
        );
    },

};
