export default class CustomProton {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.start = this.start.bind(this)
    }

    start() {
        let canvas = document.createElement('canvas')
        this.map.getCanvasContainer().appendChild(canvas)
        canvas.width = this.map.getCanvas().clientWidth
        canvas.height = this.map.getCanvas().clientHeight
        canvas.tabIndex = 1
        canvas.style.width = canvas.width+"px"
        canvas.style.height = canvas.height+"px"
        canvas.style.position = "absolute"
        canvas.style['z-index'] = 1
        let gl = canvas.getContext('experimental-webgl')

        //let canvas = this.map.getCanvas()
        //let gl = this.map.painter.gl

        let proton
        let renderer

        Main()
        function Main() {
            createProton()
            tick()
        }

        function createProton() {
            proton = new Proton
            createImageEmitter()
            createColorEmitter()

            renderer = new Proton.Renderer('webgl', proton, canvas)
            renderer.onProtonUpdate = function() {
                gl.clearColor(0, 0, 0, 0)
                gl.clear(this.gl.COLOR_BUFFER_BIT | this.gl.DEPTH_BUFFER_BIT)
            }
            renderer.start()
        }

        function createImageEmitter() {
            let emitter = new Proton.Emitter()
            emitter.rate = new Proton.Rate(new Proton.Span(32, 37), new Proton.Span(.2, .5))
            emitter.addInitialize(new Proton.Mass(1))
            emitter.addInitialize(new Proton.Life(10, 5))
            emitter.addInitialize(new Proton.ImageTarget(['/mapspotter/app/lib/proton/2.1.0/example/render/dom/image/fox.png', '/mapspotter/app/lib/proton/2.1.0/example/render/dom/image/safari.png', '/mapspotter/app/lib/proton/2.1.0/example/render/dom/image/chrome.png'], 100, 100))
            emitter.addInitialize(new Proton.Radius(40))
            emitter.addInitialize(new Proton.V(new Proton.Span(40, 8), new Proton.Span(0, 40, true), 'polar'))
            emitter.addBehaviour(new Proton.Alpha(1, 0))
            emitter.addBehaviour(new Proton.Scale(.7, 0))
            emitter.addBehaviour(new Proton.Gravity(5.5))
            emitter.addBehaviour(new Proton.Color('random'))
            emitter.addBehaviour(new Proton.Rotate(new Proton.Span(0, 360), new Proton.Span(-15, 15), 'add'))
            emitter.addBehaviour(new Proton.CrossZone(new Proton.RectZone(0, 0, 1003, 610), 'dead'))
            emitter.p.x = 1003 / 2
            emitter.p.y = 610 / 2
            emitter.emit()
            proton.addEmitter(emitter)
        }

        function createColorEmitter() {
            let emitter = new Proton.Emitter()
            emitter.rate = new Proton.Rate(new Proton.Span(16, 19), new Proton.Span(.1, .2))
            emitter.addInitialize(new Proton.Mass(1))
            emitter.addInitialize(new Proton.Radius(1, 20))
            emitter.addInitialize(new Proton.Life(1, 2))
            emitter.addInitialize(new Proton.V(new Proton.Span(1, 4), new Proton.Span(0, 360), 'polar'))
            emitter.addBehaviour(new Proton.Alpha(1, 0))
            emitter.addBehaviour(new Proton.Scale(1, 0))
            emitter.addBehaviour(new Proton.Color('random'))
            emitter.addBehaviour(new Proton.CrossZone(new Proton.CircleZone(1003 / 2, 610 / 2, 250), 'dead'))
            emitter.p.x = 1003 / 2
            emitter.p.y = 610 / 2
            emitter.emit()
            proton.addEmitter(emitter)
        }

        function tick() {
            requestAnimationFrame(tick)
            proton.update()
        }
    }
}