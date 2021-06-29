const model = require("../model/group");
const {KEY_SECRET} = require("../../config/key");
const jwt = require('jsonwebtoken');
const user = require("../model/user");
const fs = require('fs')
const modelNotice = require("../model/notification");

function writeFileImage(fileName, dataBase64) {
    fs.writeFile(fileName, dataBase64, 'base64', function (err) {
        console.log(err);
    });
}

function getFilenameImage(userId) {
    let now = new Date().getTime()
    let nameFile = `${userId}${now}`;
    return {
        path: `public/image/${nameFile}.png`,
        path_save: `/image/${nameFile}.png`
    }
}

module.exports = {
    create: (req, res) => {
        //let userId = req.params.user_id;
        var userId = jwt.verify(req.params.token, KEY_SECRET).user_id;
        let str = req.body.members
        let members = Array.from(str.slice(1, str.length - 1).split(`,`))
        let group = {
            name: req.body.name,
            members: members,
            create_date: require('moment')().format('YYYY-MM-DD HH:mm:ss'),
        }

        if (req.body.avatar !== undefined) {
            let nameFile = getFilenameImage(userId)
            writeFileImage(nameFile.path, req.body.avatar)
            group.avatar = nameFile.path_save
        }

        console.log("group", group)
        model.create(group, userId, (rs) => {
            console.log("group", rs)
            res.json(rs)
        })
    },
    update: (req, res) => {
        var userId = jwt.verify(req.params.token, KEY_SECRET).user_id;
        let group = {
            id: req.body.id,
            name: req.body.name
        }
        console.log("xxxxxxx---", req.body)
        console.log("xxxxxxx", req.body.avatar)

        if (req.body.avatar !== undefined && req.body.avatar !== null) {
            let nameFile = getFilenameImage(userId)
            writeFileImage(nameFile.path, req.body.avatar)
            group.avatar = nameFile.path_save
        }
        model.update(group, (rs) => {
            console.log("updaate", rs)
            res.json(rs)
        })
    },
    addMemberToGroup: (req, res) => {
        var memberId = jwt.verify(req.params.token, KEY_SECRET).user_id;
        let group = {
            id: req.body.id,
            userId: req.body.user_id,
            createDate: require('moment')().format('YYYY-MM-DD HH:mm:ss'),
        }
        model.addMemberToGroup(group.id, group.userId, memberId, group.createDate, (rs) => {
            let notification = {
                type: "1",
                content: group.id,
                senderId: memberId,
                userId: group.userId,
                createDate: require('moment')().format('YYYY-MM-DD HH:mm:ss')
            }
            console.log("xxxxxxxx notification", notification)
            modelNotice.createNotification(notification,(rs)=>{})
            console.log("add member", rs)
            res.json(rs)
        })
    },

    deleteMemberToGroup: (req, res) => {
        var memberId = jwt.verify(req.params.token, KEY_SECRET).user_id;
        let group = {
            id: req.body.id,
            userId: req.body.user_id
        }
        model.deleteMemberToGroup(group.id, group.userId, (rs) => {
            modelNotice.deleteNotificationByUserIdGroup( group.userId,group.id)
            res.json(rs)
        })
    },
    deleteGroup: (req, res) => {
        var user = jwt.verify(req.params.token, KEY_SECRET).user_id;
        let group = {
            id: req.params.id,
        }
        model.deleteGroup(group.id, (rs) => {
            modelNotice.deleteNotificationByContentGroup(group.id, () => {
            })
            res.json(rs)
        })
    }
}
