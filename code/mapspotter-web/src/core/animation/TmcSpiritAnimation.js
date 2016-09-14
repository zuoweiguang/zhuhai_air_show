import AnimationTool from '../../utils/AnimationTool'
import env from '../env'

export default class TmcSpiritAnimation {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.start = this.start.bind(this)
        this.endAnimate = this.endAnimate.bind(this)
        this.animationTimeInterval = 100
    }

    start() {
        let map = this.map
        let _animationTimeInterval = this.animationTimeInterval
        let k = 1
        env.tmcAnimation.timeIntervalSet.add(setInterval(()=> {
            if (k > 20) {
                k = 1
            }
            for (let j = 0; j <= 5; j++) {
                map.setFilter("TmcSpiritAnimationPoint" + j, ["all", ["==", "tmc", j], ["==", "pos", k]])
            }
            k++
        }, _animationTimeInterval))
    }

    endAnimate() {
        try {
            env.tmcAnimation.timeIntervalSet.forEach((v, k) => {
                if (v) {
                    clearInterval(v)
                }
            })
            env.tmcAnimation.timeIntervalSet = new Set()
        } catch (e) {
            console.log(e)
        }
    }
}