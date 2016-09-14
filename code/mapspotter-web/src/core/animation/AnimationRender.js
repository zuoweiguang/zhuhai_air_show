import env from '../env'
import AnimationTool from '../../utils/AnimationTool'
import TmcAnimationRender from './TmcAnimationRender'
import BrandAnimationRender from './BrandAnimationRender'
import TrafficAnimationRender from './TrafficAnimationRender'

const animationRender ={

    addTmcSpiritAnimationLayer() {
        env.mapAnimationTimeout.push(setTimeout(()=> {
            let tmc = new TmcAnimationRender(env.map, null)
            tmc.init()
            tmc.start()
        }, 1000))
    },
    addTmcBrandAnimationLayer() {
        env.mapAnimationTimeout.push(setTimeout(()=> {
            let bar = new BrandAnimationRender(env.map, null)
            bar.init()
            bar.start()
        }, 900))
    },
    addTrafficAnimationLayer() {
        env.mapAnimationTimeout.push(setTimeout(()=> {
            let traffic = new TrafficAnimationRender(env.map, null)
            traffic.init()
            //traffic.start()
        }, 800))
    },
    clearTmcSpiritAnimationLayer(){
        env.tmcAnimation.timeoutSet.forEach((v, k) => {
            if (v) {
                clearTimeout(v)
            }
        })
        env.tmcAnimation.timeIntervalSet.forEach((v, k) => {
            if (v) {
                clearInterval(v)
            }
        })
        env.tmcAnimation.timeFrameSet.forEach((v, k) => {
            if (v) {
                AnimationTool.cancelFrame(v)
            }
        })
        env.tmcAnimation.timeoutSet = new Set()
        env.tmcAnimation.timeIntervalSet = new Set()
        env.tmcAnimation.timeFrameSet = new Set()

        let mapLayerIds = Object.keys(env.map.style._layers)
        mapLayerIds.map(layerId=> {
            if (layerId.startsWith("TmcSpiritAnimation")) {
                if (env.map.getLayer(layerId)) {
                    env.map.removeLayer(layerId)
                }
            }
        })
    },

    clearTmcBrandAnimationLayer(){
        env.brandAnimation.timeoutSet.forEach((v, k) => {
            if (v) {
                clearTimeout(v)
            }
        })
        env.brandAnimation.timeIntervalSet.forEach((v, k) => {
            if (v) {
                clearInterval(v)
            }
        })
        env.brandAnimation.timeFrameSet.forEach((v, k) => {
            if (v) {
                AnimationTool.cancelFrame(v)
            }
        })
        env.brandAnimation.timeoutSet = new Set()
        env.brandAnimation.timeIntervalSet = new Set()
        env.brandAnimation.timeFrameSet = new Set()

        let mapLayerIds = Object.keys(env.map.style._layers)
        mapLayerIds.map(layerId=> {
            if (layerId.startsWith("TmcBrandAnimation")) {
                if (env.map.getLayer(layerId)) {
                    env.map.removeLayer(layerId)
                }
            }
        })
    },

    clearTrafficAnimationLayer(){
        env.trafficAnimation.timeoutSet.forEach((v, k) => {
            if (v) {
                clearTimeout(v)
            }
        })
        env.trafficAnimation.timeIntervalSet.forEach((v, k) => {
            if (v) {
                clearInterval(v)
            }
        })
        env.trafficAnimation.timeFrameSet.forEach((v, k) => {
            if (v) {
                AnimationTool.cancelFrame(v)
            }
        })
        env.trafficAnimation.timeoutSet = new Set()
        env.trafficAnimation.timeIntervalSet = new Set()
        env.trafficAnimation.timeFrameSet = new Set()

        let mapLayerIds = Object.keys(env.map.style._layers)
        mapLayerIds.map(layerId=> {
            if (layerId.startsWith("TrafficStatusAnimation")) {
                if (env.map.getLayer(layerId)) {
                    env.map.removeLayer(layerId)
                }
            }
        })
    },

}

export default animationRender