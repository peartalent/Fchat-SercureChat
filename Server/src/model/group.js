const {connection} = require("../../db/conectionDB");
module.exports = {
    getById:(id,callback)=>{
        let sql = `select * from \`group\` where group_id = ${id}`
        connection.query(sql,(err,rs)=>{
            if (!err){
                console.log("group",rs)
                callback(rs)
            } else console.log(sql,err)
        })
    }
    ,
    create: (group, idUserCreate, callback) => {
        let sql1 = ''
        if (group.avatar === undefined) {
            sql1 = `INSERT INTO \`group\` (name, create_date) VALUES ('${group.name}', '${group.create_date}')`
        } else {
            sql1 = `INSERT INTO \`group\` (name, create_date,avatar) VALUES ('${group.name}', '${group.create_date}', '${group.avatar}')`
        }
        connection.query(sql1,
            (err, rs1) => {
                if (!err) {
                    let sql2 = `INSERT INTO group_member (group_id, user_id, role, create_date) VALUES (${rs1.insertId}, '${idUserCreate}', 'admin', '${group.create_date}')`
                    connection.query(sql2,
                        (err2, rs2) => {
                            if (!err2) {
                                connection.query(`INSERT INTO \`fchat\`.\`message\` (\`create_date\`, \`type\`, \`content\`) VALUES ('${group.create_date}', 'other', 'create')`,
                                    (err4, rs4) => {
                                        if (!err4) {
                                            connection.query(`INSERT INTO \`fchat\`.\`group_message\` (\`group_receiver_id\`, \`msg_id\`, \`sender_id\`) VALUES ( ${rs1.insertId},  ${rs4.insertId}, '${idUserCreate}')`,
                                                (err5) => {
                                                    if (!err5) {
                                                        callback({
                                                            code: rs1.insertId,
                                                            status: 1
                                                        });
                                                    } else {
                                                        callback({
                                                            code: rs1.insertId,
                                                            status: 0
                                                        });
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
                            } else {
                                callback({
                                    code: 500,
                                    status: 0
                                });
                                console.log(err2);
                            }
                        }
                    );
                    (group.members).forEach(memberId => {
                        connection.query(`INSERT INTO group_member (group_id, user_id, role, create_date) VALUES (${rs1.insertId}, '${memberId}', 'default', '${group.create_date}')`)
                    })
                } else {
                    callback({
                        code: 500,
                        status: 0
                    });
                    console.log(err);
                }

            }
        );
    },
    update: function (group, callback) {
        let sql = ""
        if (group.avatar !== undefined && group.avatar !== null) {
            sql = `UPDATE \`fchat\`.\`group\` SET \`name\` = '${group.name}', \`avatar\` = '${group.avatar}' WHERE \`group_id\` = ${group.id}`
        } else {
            sql = `UPDATE \`fchat\`.\`group\` SET \`name\` = '${group.name}' WHERE \`group_id\` = ${group.id}`
        }
        console.log(sql)
        connection.query(sql, (err, rs, fields) => {
            if (!err) {
                callback({
                    code: 400,
                    status: 1
                });
            } else {
                console.log(err);
                callback({
                    code: 400,
                    status: 0
                });
            }
        });
    },
    // member là tác nhân, user là người đc thêm
    addMemberToGroup: function (groupId, userId, memberId, createDate, callback) {
        let sql1 = `INSERT INTO \`fchat\`.\`group_member\` (\`group_id\`, \`user_id\`, \`role\`, \`create_date\`) VALUES (${groupId}, '${userId}', 'default', '${createDate}')`
        connection.query(sql1, (err, rs, fields) => {
            if (!err) {
                connection.query(`INSERT INTO \`fchat\`.\`message\` (\`create_date\`, \`type\`, \`content\`) VALUES ('${createDate}', 'other', '${memberId}:add:${userId}')`,
                    (err4, rs4) => {
                        if (!err4) {
                            connection.query(`INSERT INTO \`fchat\`.\`group_message\` (\`group_receiver_id\`, \`msg_id\`, \`sender_id\`) VALUES ( ${groupId},  ${rs4.insertId}, '${userId}')`,
                                (err5) => {
                                    if (!err5) {
                                        callback({
                                            code: 2000,
                                            status: 1
                                        });
                                    } else {
                                        callback({
                                            code: 2000,
                                            status: 0
                                        });
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
            } else {
                {
                    callback({
                        code: 2000,
                        status: 0
                    });
                }
                console.log(err);
            }
        });
    },
    deleteMemberToGroup:(groupId,userId,callback)=>{
       let sql = `DELETE FROM \`fchat\`.\`group_member\` WHERE \`group_id\` = ${groupId} AND \`user_id\` = '${userId}'`
        connection.query(sql, (err, rs, fields) => {
            if (!err) {
                callback({
                    code: 400,
                    status: 1
                });
            } else {
                console.log(err,sql);
                callback({
                    code: 400,
                    status: 0
                });
            }
        });
    },
    deleteGroup:(groupId,callback)=>{
        let sql = `DELETE FROM \`fchat\`.\`group\` WHERE \`group_id\` = ${groupId}`
        connection.query(sql, (err, rs, fields) => {
            if (!err) {
                callback({
                    code: 400,
                    status: 1
                });
            } else {
                console.log(err,sql);
                callback({
                    code: 400,
                    status: 0
                });
            }
        });
    },
};