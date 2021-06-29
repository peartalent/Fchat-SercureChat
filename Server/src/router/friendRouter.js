const express = require("express")
const router = express.Router()
const controller = require("../controller/friendController")

router.get("/:token/friend/", controller.getFriendsByUserId)
router.get("/friend/", controller.getFriends)
router.delete("/:token/friend/delete/:id2", controller.unFriendById)
router.get("/:token/search/:name",controller.search)
module.exports = router