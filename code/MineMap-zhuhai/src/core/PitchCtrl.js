export default class PitchCtrl {
    constructor(map) {
        this.map = map
        this.getMousePos = this.getMousePos.bind(this)
        this.tiltMap = this.tiltMap.bind(this)
        this.pitchMap = this.pitchMap.bind(this)
        this.init = this.init.bind(this)
        this.destory = this.destory.bind(this)
        this.is3D = true
    }

    getMousePos(canvas, evt) {
        let rect = canvas.getBoundingClientRect()
        return {
            x: evt.originalEvent.clientX - rect.left,
            y: evt.originalEvent.clientY - (canvas.height / 2)
        }
    }

    tiltMap(canvas, mousePos) {
        let pitchAngle = mousePos * (120 / canvas.height);
        if (pitchAngle < 0) {
            this.map.setPitch(this.map.getPitch() + 1)
        } else {
            if (this.map.getPitch() > 1) {
                this.map.setPitch(this.map.getPitch() - 1)
            }
        }
    }

    pitchMap(e) {
        let canvas = this.map.getCanvas()
        let mousePos = this.getMousePos(canvas, e)
        this.tiltMap(canvas, mousePos.y)
    }

    init() {
        this.map.on('drag', this.pitchMap, this)
    }

    destory() {
        this.map.off('drag', this.pitchMap, this)
    }
}