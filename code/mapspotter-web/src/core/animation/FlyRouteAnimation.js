export default class FlyRouteAnimation {
    constructor(map, options, speed = 0.05) {
        this.map = map
        this.options = options || {}
        this.flyRoutes = [{
            bearing: 70.5,
                center: [116.377252,39.986506],
                zoom: 14,
                pitch: 55,
                speed: speed,
                curve: 1
        }, {
            bearing: -50,
                duration: 6000,
                center: [116.353777, 40.02324],
                bearing: 150,
                zoom: 14.8,
                pitch: 30,
                speed: speed,
                curve: 1
        },{
            bearing: -10,
                center: [116.499118, 40.000075],
                zoom: 14.2,
                pitch: 40,
                speed: speed,
                curve: 1
        },{
            bearing: 90,
                center: [116.488197, 39.87509],
                zoom: 13.5,
                speed: speed,
                pitch: 60,
                curve: 1
        },{
            bearing: 150,
                center: [116.439377, 39.870708],
                zoom: 15.3,
                pitch: 20,
                speed: speed,
                curve: 1
        },{
            bearing: 180,
                center: [116.344345, 39.859543],
                zoom: 12.3,
                pitch: 60,
                speed: speed,
                curve: 1
        }, {
            bearing: 45,
                center: [116.310229, 39.922166],
                zoom: 14.3,
                pitch: 40,
                speed: speed,
                curve: 1
        },{
            bearing: 0,
                center: [116.278151, 39.875484],
                zoom: 14.3,
                pitch: 20,
                speed: speed,
                curve: 1
        }]
    }

    updateOptions(options) {
        this.options = options
    }

    init() {
        let map = this.map
        let that = this
        that.options.mapFlyAction(false)
        that.options.mapFlyStepAction(0)
        map.on("moveend", function () {
            if (that.options && that.options.map.init.fly) {
                let flyStep = that.options.map.init.flyStep
                if (flyStep > 5) {
                    that.options.mapFlyAction(false)
                    that.options.mapFlyStepAction(0)
                    return;
                }

                (function () {
                    setTimeout(function () {
                        map.flyTo(that.flyRoutes[flyStep])
                    }, 1000)
                })()
                that.options.mapFlyStepAction(flyStep + 1)
            }
        })
    }

    destory() {
        this.map = null
        this.options = null
        this.flyRoutes = null
    }
}


