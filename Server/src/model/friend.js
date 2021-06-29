const {connection } = require("../../db/conectionDB");
module.exports = {
    create: ( userId1,userId2,creatDate) => {
        let sql =`INSERT INTO \`fchat\`.\`user_friend\` (\`user1_id\`, \`user2_id\`, \`create_date\`) VALUES ('${userId1}', '${userId2}', '${creatDate}')`
        connection.query(
            sql
            , (err, rs, fields) => {
                if (!err) {
                    console.log(rs);
                } else console.log(err);
            });
    },
    get: ( callback) => {
        let sql =`SELECT f.user1_id,u1.fullname name1, u1.avartar avatar1,u1.last_online last_online1,
        u1.status status1,f.user2_id, u2.fullname name2,u2.avartar avatar2,u2.last_online last_online2, u1.sex sex1. u2.sex sex2
            u2.status status2, f.create_date FROM user_friend f, user u1, user u2
        WHERE u1.user_id = f.user1_id and u2.user_id = f.user2_id;`
        connection.query(
            sql
            , (err, rs, fields) => {
            if (!err) {
                console.log(rs)
                callback(rs);
            } else console.log(err);
        });
    },
    getById: function (id, callback) {
        connection.query(`
        SELECT f.user1_id,u1.fullname name1, u1.avartar avatar1,u1.last_online last_online1,u1.status status1,u1.public_key public_key1,f.user2_id, u2.fullname name2,u2.avartar avatar2,u2.last_online last_online2,u2.status status2,u2.public_key public_key2, f.create_date,u1.sex sex1,u2.sex sex2 FROM user_friend f, user u1, user u2 WHERE u1.user_id = f.user1_id and u2.user_id = f.user2_id and (user1_id="${id}" or user2_id ="${id}")
        `, (err, rs, fields) => {
            if (!err) {
                callback(rs);
            } else console.log(err);
        });
    },
    // hàm check 2 người có phải bạn không
    getByTwoId:(userId1,userId2,callback)=>{
       let sql = `SELECT * FROM user_friend WHERE (user1_id = '${userId1}' and user2_id ='${userId2}') or (user1_id = '${userId2}' and user2_id ='${userId1}')`
        connection.query(
            sql, (err, rs) => {
                if (!err) {
                    console.log("get by two id",rs)
                    callback(rs);
                } else console.log(err,sql);
            });
    },
    unfriendById: (userId1,userId2,callback)=>{
        let sql = `DELETE  FROM user_friend WHERE (user1_id = '${userId1}' and user2_id ='${userId2}') or (user1_id = '${userId2}' and user2_id ='${userId1}')`
        connection.query(
            sql, (err, rs) => {
                if (!err) {
                    callback({
                        status:1,
                        code:3223
                    });
                } else {
                    console.log(err,sql);
                    callback({
                        status:0,
                        code:3223
                    });
                }
            });
    },
    searchByName:(name,id,callback)=>{
        let sql = `SELECT * from \`user\` WHERE user_id != '${id}' and fullname LIKE '%${name}%'`
        connection.query(
            sql, (err, rs) => {
                if (!err) {
                    callback(rs);
                } else console.log(err,sql);
            });
    }
};