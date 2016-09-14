export default class CustomAnimation {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.scene = null
        this.camera = null
        this.renderer = null
        this.initThree = this.initThree.bind(this)
        this.initCamera = this.initCamera.bind(this)
        this.initScene = this.initScene.bind(this)
        this.initLight = this.initLight.bind(this)
        this.initObject = this.initObject.bind(this)
        this.start = this.start.bind(this)
    }

    initThree() {
        let width = this.map.getCanvas().clientWidth
        let height = this.map.getCanvas().clientHeight
        this.renderer = new THREE.WebGLRenderer({
            antialias: true,
            alpha:true
        })
        this.renderer.setSize(width, height)
        let d = this.renderer.domElement
        d.style.position = 'absolute'
        d.tabIndex = 1
        let c = document.createElement('div');
        c.style.zIndex = 4
        c.appendChild(d)
        this.map.getCanvasContainer().appendChild(d)
        this.renderer.setClearColor(0x000000,0)
    }

    initCamera() {
        let width = this.map.getCanvas().clientWidth
        let height = this.map.getCanvas().clientHeight
        this.camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 500)
        this.camera.position.x = 0
        this.camera.position.y = 300
        this.camera.position.z = 200
        this.camera.up.x = 0
        this.camera.up.y = 1
        this.camera.up.z = 0
        this.camera.lookAt({
            x: 0,
            y: 0,
            z: 0
        })
    }

    initScene() {
        this.scene = new THREE.Scene()
    }

    initLight() {
        let light = new THREE.AmbientLight(0xFFFFFF)
        light.position.set(0, 0, 0)
        this.scene.add(light)
    }

    initObject() {
        let geometry = new THREE.Geometry()
        geometry.vertices.push(new THREE.Vector3(-500, 0, 0))
        geometry.vertices.push(new THREE.Vector3(500, 0, 0))

        for (let i = 0; i <= 20; i++) {
            let line = new THREE.Line(geometry, new THREE.LineBasicMaterial({color: 0x000000}))
            line.position.z = ( i * 50 ) - 500
            this.scene.add(line)

            let line1 = new THREE.Line(geometry, new THREE.LineBasicMaterial({color: 0xFFFFFF}))
            line1.position.x = ( i * 50 ) - 500
            line1.rotation.y = 90 * Math.PI / 180
            this.scene.add(line1)
        }
    }

    start() {
        this.initThree()
        this.initCamera()
        this.initScene()
        this.initLight()
        this.initObject()
        this.renderer.clear()
        this.renderer.render(this.scene, this.camera)
    }
}