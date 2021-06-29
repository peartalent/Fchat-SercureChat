const messageDB = require("../model/message");
const friendDB = require("../model/friend");
const {KEY_SECRET} = require("../../config/key");
const jwt = require('jsonwebtoken');
const {MESSAGE_TYPE_OFF_IMAGE} = require("../../config/type_data");
const fs = require('fs')
Array.prototype.containUser = function (obj) {
    var i = this.length;
    while (i--)
        if (this[i].user_id == obj.user_id)
            return true;
    return false;
}
Array.prototype.containMessage = function (obj) {
    var i = this.length;
    while (i--)
        if (this[i].msg_id == obj.msg_id)
            return true;
    return false;
}
module.exports = {
    getAll: (req, res) => {
        messageDB.get(rs => {
            res.json(rs)
        })
    },
    getAllMesageById: (req, res) => {
        let id = req.params.id
        messageDB.get(rs => {
            let x = rs.filter(message => id === message.sender_id || id === message.receiver_id)
            res.json(x)
        })
    },
    getShortMesageById: (req, res) => {
        let id = req.params.id
        messageDB.get(rs => {
            let x = rs.filter(message => id === message.sender_id || id === message.receiver_id)
            res.json(x)
        })
    },
    getMessageUserByUserId: function (req, res) {
        let token = req.params.token
        let id2 = req.params.id2
        let user = jwt.verify(token, KEY_SECRET);
        let id1 = user.user_id
        messageDB.getMessageUserByUserId(id1, id2, (rs) => {
            let results = []
            rs.forEach(row => {
                let isSender = false
                // CODE NGU VIET NGUOC
                if (id1 === row.sender_id) {
                    isSender = false
                } else {
                    isSender = true
                }
                let message = {
                    msg_id: row.msg_id,
                    create_date: row.create_date,
                    type: row.type,
                    content: row.content,
                    status: row.msg_status,
                    is_sender: isSender,
                }
                if(row.type === "img"){
                   // let url =  "../../../public"+row.content
                    let url ="public"+message.content
                    try {
                        message.content = fs.readFileSync(url, 'base64')
                        console.log("ma hoa ne")
                    } catch (e) {
                       console.log(e)
                    }
                }
                results.push(message)
            })
            res.json(results);
        });
    },
    udStatusMessageUserByUserId:function (req, res) {
        let token = req.params.token
        let id2 = req.body.id
        let user = jwt.verify(token, KEY_SECRET);
        let id1 = user.user_id
        messageDB.getMessageUserByUserId(id1, id2,  (rs) => {
            rs.forEach(row => {
                if (id2 === row.receiver_id) {
                    messageDB.updateStatusMessage(row.msg_id,()=>{
                        console.log("ud status message",row.msg_id)
                    })
                }
            })
            res.json({
                status :1,
                code:111
            });
        });
    },
    getMessageImageUserByUserId: function (req, res) {
        let token = req.params.token
        let id2 = req.params.id2
        let user = jwt.verify(token, KEY_SECRET);
        let id1 = user.user_id
        messageDB.getMessageUserByUserId(id1, id2, (rs) => {
            let results = []
            rs.forEach(row => {
                if (row.type === MESSAGE_TYPE_OFF_IMAGE) {
                    let message = {
                        msg_id: row.msg_id,
                        create_date: row.create_date,
                        type: row.type,
                        content: row.content,
                        status: row.msg_status
                    }
                }
                results.push(message)
            })
            res.json(results);
        });
    },
    getMemberOfGroupById: (groupId, callback) => {
        messageDB.getGroupByGroupId(groupId, (rs) => {
            let members = []
            rs.forEach(row => {
                let member = {
                    user_id: row.member_id,
                    fullname: row.member_name,
                    last_online: row.member_last_online,
                    avatar: row.member_avatar,
                    token_client: row.member_token,
                    role: row.member_role,
                    create_date: row.member_create_date,
                    public_key: row.member_public_key
                }
                if (!members.containUser(member)) {
                    members.push(member)
                }
            })
            callback(members)
        });
    },
    getBase65Image:(req,res) =>{

    }
    ,
    getGroupByGroupId: (req, res) => {
        let id = jwt.verify(req.params.token, KEY_SECRET).user_id;
        let groupId = req.params.group_id
        messageDB.getGroupByGroupId(groupId, (rs) => {
            let result = {}
            let members = []
            let messages = []
            rs.forEach(row => {
                let member = {
                    user_id: row.member_id,
                    fullname: row.member_name,
                    last_online: row.member_last_online,
                    avatar: row.member_avatar,
                    token_client: row.member_token,
                    role: row.member_role,
                    create_date: row.member_create_date,
                    public_key: row.member_public_key
                }
                let message = {
                    msg_id: row.msg_id,
                    create_date: row.create_date,
                    type: row.type,
                    content: row.content,
                    status: row.status,
                    is_sender: id === row.sender_id ? true : false,
                    user: {
                        user_id: row.sender_id,
                        fullname: row.sender_name,
                        last_online: row.member_last_online,
                        avatar: row.sender_avatar,
                        token_client: row.sender_id_token,
                        public_key: row.sender_public_key
                    }
                }
                if(row.type === "img"){
                    // let url =  "../../../public"+row.content
                    let url ="public"+message.content
                    try {
                        message.content = fs.readFileSync(url, 'base64')
                        console.log("ma hoa ne")
                    } catch (e) {
                        console.log(e)
                    }
                }

                if (!members.containUser(member)) {
                    members.push(member)
                }
                if (!messages.containMessage(message)) {
                    messages.push(message)
                }
                result = {
                    group_id: row.group_id,
                    name: row.name,
                    avatar: row.avatar,
                    create_date: row.group_create_date,
                    messages: messages,
                    members: members
                }
            })
            result.messages = messages.sort(function (a, b) {
                var dateA = new Date(a.create_date);
                var dateB = new Date(b.create_date);
                if (dateA > dateB) {
                    return -1;
                }
                if (dateA < dateB) {
                    return 1;
                }
                return 0;
            });
            result.members = members
            res.json(result);
        });
    }
    ,
    getAllGroupByMemberId: (req, res) => {
        let id = jwt.verify(req.params.token, KEY_SECRET).user_id;
        messageDB.getAllGroupByMemberId(id, async (groupIds) => {
            var groups = []
            for (const row of groupIds) {
                await new Promise(resolve => {
                    messageDB.getGroupByGroupId(row.group_id, (groupsDB) => {
                        let result = {}
                        let members = []
                        let messages = []
                        groupsDB.forEach(row => {
                            let member = {
                                user_id: row.member_id,
                                fullname: row.member_name,
                                last_online: row.member_last_online,
                                avatar: row.member_avatar,
                                token_client: row.member_token,
                                role: row.member_role,
                                create_date: row.member_create_date,
                                public_key: row.member_public_key
                            }
                            let message = {
                                msg_id: row.msg_id,
                                create_date: row.create_date,
                                type: row.type,
                                content: row.content,
                                status: row.status,
                                is_sender: id === row.sender_id ? true : false,
                                user: {
                                    user_id: row.sender_id,
                                    fullname: row.sender_name,
                                    last_online: row.member_last_online,
                                    avatar: row.sender_avatar,
                                    token_client: row.sender_id_token,
                                    public_key: row.sender_public_key
                                }
                            }

                            if (!members.containUser(member)) {
                                members.push(member)
                            }
                            if (!messages.containMessage(message)) {
                                messages.push(message)
                            }
                            result = {
                                group_id: row.group_id,
                                name: row.name,
                                avatar: row.avatar,
                                create_date: row.group_create_date,
                                messages: messages,
                                members: members
                            }
                        })
                        result.messages = messages.sort(function (a, b) {
                            var dateA = new Date(a.create_date);
                            var dateB = new Date(b.create_date);
                            if (dateA > dateB) {
                                return -1;
                            }
                            if (dateA < dateB) {
                                return 1;
                            }
                            return 0;
                        });
                        result.members = members
                        groups.push(result)
                        resolve(true);
                    })
                })
            }
            console.log("xxxxxxxxxxxxxxx", groups)
            res.json(groups);
        });
    },
    sendMessageById: (message, callback) => {
        messageDB.sendMessageById(message, (rs) => {
            callback(rs)
        })
    },
    sendMessageGroup: (message, callback) => {
        messageDB.sendMessageToGroupById(message, (rs) => {
            callback(rs)
        })
    },
    getLastTimeMessageByFriend: (req, res) => {
        let token = req.params.token
        let user = jwt.verify(token, KEY_SECRET);
        let id = user.user_id
        friendDB.getById(id, async (rs) => {
            var friends = []
            rs.forEach(friend => {
                if (friend.user1_id === id) {
                    let u = {
                        user_id: friend.user2_id
                    }
                    friends.push(u)
                } else if (friend.user2_id === id) {
                    let u = {
                        user_id: friend.user1_id,
                    }
                    friends.push(u)
                }
            })
            var results = []
            for (const friend of friends) {
                await new Promise(resolve => {
                    messageDB.getLastTimeMessageByFriend(id, friend.user_id, (rs1) => {
                        console.log("length rs", rs1.length)
                        if (rs1.length > 0) {
                            let isSender = false
                            let user = {}
                            console.log(rs1[0])
                            if (id === rs1[0].sender_id) {
                                user = {
                                    user_id: rs1[0].receiver_id,
                                    fullname: rs1[0].name2,
                                    avatar: rs1[0].avatar2,
                                    status: rs1[0].status2,
                                    last_online: rs1[0].last_online2,
                                    public_key: rs1[0].public_key2
                                }
                                isSender = false
                            } else {
                                user = {
                                    user_id: rs1[0].sender_id,
                                    fullname: rs1[0].name1,
                                    avatar: rs1[0].avatar1,
                                    status: rs1[0].status1,
                                    last_online: rs1[0].last_online1,
                                    public_key: rs1[0].public_key1
                                }
                                isSender = true
                            }
                            let shortMessage = {
                                msg_id: rs1[0].msg_id,
                                create_date: rs1[0].create_date,
                                type: rs1[0].type,
                                content: rs1[0].content,
                                status: rs1[0].msg_status,
                                is_sender: isSender,
                                user: user
                            }
                            results.push(shortMessage)
                        }
                        resolve(true);
                    })
                });

            }
            res.json(results)
        })
    },
    deleteMessage: (msgId,callback) => {
        messageDB.deleteMessage(msgId, (rs) => {
            console.log("delete messge",rs)
            callback(rs)
        })
    },
    deleteAllMessage:(id1,id2,callback) => {
        messageDB.deleteAllMessage(id1,id2, (rs) => {
            console.log("delete all messge",rs)
            callback(rs)
        })
    },
};
