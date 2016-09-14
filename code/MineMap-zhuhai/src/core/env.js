import solutionHandler from '../middleware/handler/solutionHandler'
const env = {
    map: null,
    style: null,
    mapAnimationTimeout: [],
    mapAnimationTimeInterval: [],
    frameIdSet: new Set(),
    trafficAnimation: {
        timeoutSet: new Set(),
        timeIntervalSet: new Set(),
        timeFrameSet: new Set()
    },
    tmcAnimation: {
        timeoutSet: new Set(),
        timeIntervalSet: new Set(),
        timeFrameSet: new Set()
    },
    brandAnimation: {
        timeoutSet: new Set(),
        timeIntervalSet: new Set(),
        timeFrameSet: new Set()
    },
    operateRecords: Array.of(),
}

export default env