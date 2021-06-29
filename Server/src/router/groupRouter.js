const express = require("express")
const router = express.Router()
const controller = require("../controller/groupController")

router.post("/:token/group/create", controller.create)
router.put("/:token/group/update", controller.update)
router.post("/:token/group/add-member", controller.addMemberToGroup)
router.post("/:token/group/del-member", controller.deleteMemberToGroup)
router.delete("/:token/group/delete/:id", controller.deleteGroup)
module.exports = router