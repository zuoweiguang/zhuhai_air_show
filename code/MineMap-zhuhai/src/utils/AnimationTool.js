const frame = window.requestAnimationFrame ||
    window.mozRequestAnimationFrame ||
    window.webkitRequestAnimationFrame ||
    window.msRequestAnimationFrame

const cancel = window.cancelAnimationFrame ||
    window.mozCancelAnimationFrame ||
    window.webkitCancelAnimationFrame ||
    window.msCancelAnimationFrame;

const AnimationTool = {
    frame(fn) {
        return frame(fn)
    },
    cancelFrame(id) {
        cancel(id)
    }
}

export default AnimationTool