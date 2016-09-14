import solutionService from '../middleware/service/solutionService'

export default class CustomLayer {
    constructor(options) {
        this.options = options
        this.solutionId = options.solutionId
        this.id = options.id
        this.type = options.type
        this.sourceId = options.source
        this.sourceLayerId = options["source-layer"]
        this.styleId = options.id
        this.source = {}
        this.sourceLayer = {}
        this.style = {}

        this.addTo = this.addTo.bind(this)
        this.addStyleLayer = this.addStyleLayer.bind(this)
        this.updateLayer = this.updateLayer.bind(this)
    }

    addTo(map) {
        this._map = map
        this.addStyleLayer(this.styleId)
    }

    addStyleLayer(id) {
        solutionService.getStyleInfo(this.solutionId).then(response =>
            response.json().then(res => ({res, response}))
        ).then(({ res, response }) => {
            if (response.ok && res.errcode == 0) {
                this.updateLayer(id, res.data)
            }
        }).catch((e)=>
            console.log(e)
        )
    }

    updateLayer(id, data) {
        if (data && data.length > 0) {
            let styleDatas = data.filter(style =>
                style.id === id
            )
            if (styleDatas && styleDatas.length > 0) {
                this.style = styleDatas[0]
                this._map.addLayer(this.style)
            }
        }
    }
}