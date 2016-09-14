import * as utils from '../utils/ObjectUtil'
import {APP_ROOT_NAME} from '../config/appConfig'

export default class Style {
    constructor(version = 8, name = "Bright", sprite = APP_ROOT_NAME + "app/sprite", glyphs = "mapbox://fonts/mapbox/{fontstack}/{range}.pbf", sources = {}, layers = []) {
        this.version = version
        this.name = name
        this.sprite = sprite
        this.glyphs = glyphs
        this.sources = sources
        this.layers = layers
    }

    getVersion() {
        return this.version
    }

    setVersion(version = 8) {
        this.version = version
    }

    getName() {
        return this.name
    }

    setName(name = "Bright") {
        this.name = name
    }

    getSprite() {
        return this.sprite
    }

    setSprite(sprite = APP_ROOT_NAME + "app/sprite") {
        this.sprite = sprite
    }

    getGlyphs() {
        return this.glyphs
    }

    setGlyphs(glyphs = "mapbox://fonts/mapbox/{fontstack}/{range}.pbf") {
        this.glyphs = glyphs
    }

    getSources() {
        return this.sources
    }

    setSources(sources = {}) {
        this.sources = sources
    }

    getLayers() {
        return this.layers
    }

    setLayers(layers = []) {
        this.layers = layers
    }

    addSource(source = {}) {
        this.sources = utils.extendAll(this.sources, source)
    }

    removeSource(source = {}) {
        for (let key of keys(source)) {
            delete this.sources[key]
        }
    }

    addLayer(layer = {}) {
        this.layers.push(layer)
    }

    removeLayer(id) {
        this.layers = this.layers.filter(layer =>
            layer.id !== id
        )
    }

    serialize() {
        return {
            version: this.version,
            name: this.name,
            sprite: this.sprite,
            glyphs: this.glyphs,
            sources: this.sources,
            layers: this.layers
        }
    }
}