const {connectDB, closeDB, connection} = require("../../db/conectionDB");
const {KEY_SECRET} = require("../../config/key");
const {TYPE} = require("../../config/type_data")
const jwt = require('jsonwebtoken');
module.exports = {
    get: (con, callback) => {
        con.query("select * from user", (err, rs, fields) => {
            if (!err) {
                callback(rs);
            } else console.log(err);
        });
    },
    getById: function (con, id, callback) {
        con.query(`SELECT * FROM user WHERE user_id = ${id}`, (err, rs, fields) => {
            if (!err) {
                callback(rs);
            } else console.log(err);
        });
    }
    ,
    getByPhone: function (phone, callback) {
        let sql = `SELECT * FROM user WHERE phone = '${phone}'`
        connection.query(sql
            ,
            (err, rs) => {
                if (!err) {
                    callback(rs);
                    console.log("get by phone", rs, rs.length)
                } else {
                    console.log(err);
                }
            }
        );
    },
    updatePublicKey: function (phone, publicKey, callback) {
        let sql = `UPDATE \`fchat\`.\`user\` SET \`public_key\` = '${publicKey}' WHERE phone = '${phone}'`
        connection.query(sql
            ,
            (err, rs) => {
                if (!err) {
                    callback({
                        status: 1
                    });
                } else {
                    console.log(err);
                    callback({
                        status: 0
                    });
                }
            }
        );
    },
    create: function (user, callback) {
        connection.query(
            `INSERT INTO user (user_id, phone, public_key) VALUES ('${user.user_id}','${user.phone}','${user.public_key}')`,
            (err, rs, fields) => {
                if (!err) {
                    console.log(rs)
                    callback({
                        code: 1000,
                        status: 1,
                        token: jwt.sign({user}, KEY_SECRET),
                        user_id: user.user_id
                    });
                } else callback({
                    code: 1000,
                    status: 0,
                    message: err.message
                });
            }
        );
    },
    updateStatus: (id, status, callback) => {
        let sql = `UPDATE fchat.user SET status = '${status}' WHERE user_id = '${id}';`
        connection.query(sql, (err, rs, fields) => {
                if (!err) {
                    callback({
                        code: 4000,
                        id: id,
                        status: status,
                    });
                } else console.log(err);
            }
        );
    },
    updateLastOnline: (id, last_online, callback) => {
        let sql = `UPDATE fchat.user SET last_online = '${last_online}' WHERE user_id = '${id}';`
        connection.query(sql, (err, rs, fields) => {
                if (!err) {
                    callback({
                        code: 4001,
                        last_online: last_online,
                        status: id,
                    });
                } else console.log(err);
            }
        );
    },
    getUserClientById: (id, callback) => {
        let sql = `select * from user where user_id = '${id}'`

        connection.query(sql, async (err, rs) => {
            if (!err) {
                if (rs.length >= 0) {
                    callback(rs[0]);
                }

            } else console.log(err);
        })
    },
    //eDuMjDWUQ1ubjipl838_v_:APA91bFSHV9txlVvdhNuc_QYPLOBTzP1EQt5Bwkh1eKtKtotzKHd6YyrgXzFRt6xKI29c8EBbyKtUSXnaJZ0BdIrP0EYMg7SpZEa-kTomMlPgLirOnJCGV5olSQSx8EnQC5ad0eoI82k
    updateTokenById: (id, token, callback) => {
        let sql = `UPDATE \`fchat\`.\`user\` SET \`token_client\` = '${token}' WHERE \`user_id\` = '${id}'`;
        connection.query(sql, async (err, rs) => {
            if (!err) {
                callback({
                    code: 200,
                    token_client: token,
                    status: id,
                });
            } else {
                console.log(err)
            }
        })
    },
    updateNameById: (name, id, callback) => {
        let sql = `UPDATE \`fchat\`.\`user\` SET \`fullname\` = '${name}' WHERE \`user_id\` = '${id}'`
        connection.query(sql,(e,rs)=>{
            if(!e){
                callback({
                    status:1,
                    code:200
                })
            }else {
                console.log(e)
                callback({status:0,code:200})
            }
        })
    },
    updateSexById: (sex, id, callback) => {
        let sql = `UPDATE \`fchat\`.\`user\` SET sex = '${sex}' WHERE user_id = '${id}'`
        connection.query(sql,(e,rs)=>{
            if(!e){
                console.log(sql)
                callback({
                    status:1,
                    code:200
                })
            }else {
                console.log(sql)
                console.log(e)
                callback({status:0,code:200})
            }
        })
    },
    updateAvatarById: (avatar, id, callback) => {
        let sql = `UPDATE \`fchat\`.\`user\` SET \`avartar\` = '${avatar}' WHERE \`user_id\` = '${id}'`
        connection.query(sql,(e,rs)=>{
            if(!e){
                callback({
                    status:1,
                    code:200
                })
            }else {
                console.log(e)
                callback({status:0,code:200})
            }
        })
    },
    destroy: function (con, id, callback) {
        con.query(`DELETE FROM user WHERE user_id = ${id}`, callback);
    },
};

class User {
    constructor(
        user_id,
        fullname,
        phone,
        lass_online,
        status,
        avatar,
        public_key
    ) {
        this.user_id = user_id;
        this.fullname = fullname;
        this.phone = phone;
        this.lass_online = lass_online;
        this.status = status;
        this.avatar = avatar;
        this.public_key = public_key;
    }
}
