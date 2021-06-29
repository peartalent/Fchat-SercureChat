const express = require("express")
const router = express.Router()
const messageController = require("../controller/messageController")

router.get("/:token/message/:id2", messageController.getMessageUserByUserId)
router.get("/:token/message/group/:group_id/member", messageController.getGroupByGroupId)
router.get("/:token/group/member", messageController.getAllGroupByMemberId)
// router.post("/chat", messageController.sendMessageById)
router.get("/message",messageController.getAll)
router.get("/:token/message", messageController.getLastTimeMessageByFriend)
router.put("/:token/message/update-status", messageController.udStatusMessageUserByUserId)
module.exports = router