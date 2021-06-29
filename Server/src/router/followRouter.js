const express = require("express")
const router = express.Router()
const controller = require("../controller/followController")

router.get("/:token/follow/", controller.getFollowUserByUserId)
router.post("/:token/accept-follow",controller.acceptFollow)
router.delete("/:token/:id2/refuse-follow",controller.refuseFollow)
router.delete("/:token/:id2/cancel-follow",controller.cancelFollow)
router.post("/:token/create-follow",controller.create)
module.exports = router