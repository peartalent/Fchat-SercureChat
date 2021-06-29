const user = require("../model/user");
const friendM = require("../model/friend");
const followM = require("../model/follow");
const {KEY_SECRET} = require("../../config/key");
const jwt = require('jsonwebtoken');
const multer = require('multer')
const {v4: uuidv4} = require('uuid');
// const { uuidv4 } = require('uuidv4');


module.exports = {
    getByPhone: (req, res) => {
        let token = req.params.token;
        let phone = jwt.verify(token, KEY_SECRET).phone;
        user.getByPhone(phone, (rs) => {
            console.log("get user by phone: ", rs)
            res.json(rs)
        })
    },
    checkFriendOrFollower:(req,res)=>{
        let token = req.params.token;
        let user = jwt.verify(token, KEY_SECRET)
        let userId2 = req.params.id2
        var status =0
        friendM.getByTwoId(user.user_id,userId2,(rs)=>{
            // cần thêm không có trong block list
            if (rs.length >0){
                status =1
                console.log("check nè",status)
                res.json({
                    code:200,
                    status:1
                })
            } else {
                followM.checkFollow(userId2,user.user_id,(rs2)=>{
                    if (rs2.length>0){
                        status =2
                        console.log("check nè",status)
                        res.json({
                            code:200,
                            status:2
                        })

                    } else {
                        followM.checkFollow(user.user_id,userId2,(rs3)=>{
                            if (rs3.length>0){
                                status =3
                                console.log("check nè",status)
                                res.json({
                                    code:200,
                                    status:3
                                })

                            } else {
                                status =0
                                console.log("check nè",status)
                                res.json({
                                    code:200,
                                    status:0
                                })
                            }
                        })
                    }
                })
            }
        })
    }
    ,
    getUserClientById: (id, callback) => {
        user.getUserClientById(id,
            async (rs) => {
                callback(rs)
            })
    }
    ,
    getUserById: (req, res) => {
        let id = req.params.user_id
        console.log(id)
        user.getUserClientById(id, (rs) => {
            rs.avatar = rs.avartar
            res.json(rs)
        })

    }
    ,
    logout : (req,res)=>{
        let token = req.params.token;
        let id = jwt.verify(token, KEY_SECRET).user_id;
        this.updateStatus(id, "off")
        user.updateTokenById(id,"",(rs)=>{
            console.log("logout",rs)
        })
        res.json({
            status:1,
            code:112
        })
    }
    ,
    updateTokenById: (token, token_client) => {
        user.updateTokenById(jwt.verify(token, KEY_SECRET).user_id, token_client, (rs) => {
            console.log(rs)
        })
    }
    ,
    updateNameById: (req, res) => {
        let token = req.params.token;
        let name = req.params.name;
        let id = jwt.verify(token, KEY_SECRET).user_id;
        user.updateNameById(name,id, (rs) => {
            console.log("Update name ",rs)
            res.json(rs)
        })
    }
    ,
    updateSexById: (req, res) => {
        let token = req.params.token;
        let sex = req.params.sex;
        let id = jwt.verify(token, KEY_SECRET).user_id;

        user.updateSexById(sex,id, (rs) => {
            console.log("Update sex ",rs,req.params)
            res.json(rs)
        })
    }
    ,
    updateAvatarById: (req, res) => {
        let token = req.params.token;
        let id = jwt.verify(token, KEY_SECRET).user_id;
        console.log("file",req.file);
        var path_save= `/image/${req.file.filename}`
        user.updateAvatarById(path_save,id,(rs)=>{
           res.json(rs)
        })
       // res.json
    }
    ,
    getMeByPhone: (req, res) => {
        let phone = req.params.phone;
        user.getByPhone(phone, (rs) => {
            console.log("get user by phone: ", rs)
            res.json(rs)
        })
    },
    getUserByToken: (req, res) => {
        let token = req.params.token;
        let phone = jwt.verify(token, KEY_SECRET).phone;
        user.getByPhone(phone, (users) => {
            if (users.length > 0) {
                let user = users[0]
                let result = {
                    user_id: user.user_id,
                    phone: user.phone,
                    public_key: user.public_key,
                    avatar: user.avartar,
                    fullname: user.fullname,
                    sex:user.sex
                }
                console.log("result user by tokeb", result)
                res.json(result)
            }
        })
    },
    createUser: function (req, res) {
        let u = req.body
        console.log("use create", u)
        user.getByPhone(u.phone, (users) => {
            user.updatePublicKey(u.phone,u.public_key,(rs)=>{})
            if (users.length > 0) {
                let user = users[0]
                let result = {
                    code: 1000,
                    status: 1,
                    token: jwt.sign({
                        user_id: user.user_id,
                        phone: user.phone,
                        public_key: user.public_key
                    }, KEY_SECRET),
                    user_id: user.user_id
                }
                res.json(result)
            } else {
                u.user_id = uuidv4().toString().replace(/-/g, "")
                user.create(u, (rs) => {
                    if (rs.code == 1000 && rs.status === 1) {
                        res.json(rs);
                    } else {
                        u.user_id = uuidv4().toString().replace(/-/g, "")
                        user.create(u, (rs) => {
                            if (rs.code == 1000 && rs.status === 1) {
                                res.json(rs);
                            }
                        });
                    }
                });
            }
        })
    },
    updateStatus: function (id, status) {
        user.updateStatus(id, status, (rs) => {
            console.log(rs)
        });
    },
    updateLastOnline: (id, last_online) => {
        user.updateLastOnline(id, last_online, (rs) => {
            console.log(rs)
        })
    }
}
