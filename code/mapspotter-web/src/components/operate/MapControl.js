import React, { Component, PropTypes } from 'react'
import env from '../../core/env'
import icon2d from '../../images/map/3d/2d.png'
import icon3d from '../../images/map/3d/3d.png'

export default class MapControl extends Component {
    constructor(props, context) {
        super(props, context)
        this.btnZoomInClick = this.btnZoomInClick.bind(this)
        this.btnZoomOutClick = this.btnZoomOutClick.bind(this)
        this.btnBearingClick = this.btnBearingClick.bind(this)
        this.btnPitchClick = this.btnPitchClick.bind(this)
        this.btnRestoreClick = this.btnRestoreClick.bind(this)
        this.on3DLayerVisible = this.on3DLayerVisible.bind(this)
        this.bearing = 0
        this.pitch = 60
        this.easeTo = this.easeTo.bind(this)
        this.move = this.move.bind(this)
        this.goDirection = this.goDirection.bind(this)
        this.registerKeydown = this.registerKeydown.bind(this)
        this.clearSelect = this.clearSelect.bind(this)
        this.updateButtonState = this.updateButtonState.bind(this)

        this.timeout = null
        this.state = {
            'move-forward': false,
            'move-backward': false,
            'move-left': false,
            'move-right': false,
            'rot-left': false,
            'rot-right': false,
            'pitch-up': false,
            'pitch-down': false
        }
    }

    btnZoomInClick(e) {
        e.preventDefault()
        let map = env.map
        map.zoomIn()
    }

    btnZoomOutClick(e) {
        e.preventDefault()
        let map = env.map
        map.zoomOut()
    }

    btnBearingClick(e) {
        e.preventDefault()
        let map = env.map
        this.bearing = this.bearing - 15
        if (this.bearing < -180) {
            this.bearing = 180
        }
        map.setBearing(this.bearing)
    }

    btnPitchClick(e) {
        e.preventDefault()
        let map = env.map
        if (this.pitch >= 60) {
            this.pitch = 0
            this.refs.icon3dImg.src = icon2d
            this.on3DLayerVisible(map, false)
        } else {
            this.pitch = 60
            this.refs.icon3dImg.src = icon3d
            this.on3DLayerVisible(map, true)
        }
        map.setPitch(this.pitch)
    }

    on3DLayerVisible(map, is3d) {
        this.props.solution.layers.map(layer => {
                if (layer.type == 'extrusion') {
                    let visibility = 'visible'
                    if (is3d) {
                        visibility = 'visible'
                    } else {
                        visibility = 'none'
                    }
                    this.props.actions.solutionLayerVisibleAction(layer.id, layer.layout, visibility)
                    if (map.getLayer(layer.id)) {
                        map.setLayoutProperty(layer.id, 'visibility', visibility)
                    }
                }
            }
        )
    }

    btnRestoreClick(e) {
        e.preventDefault()
        let map = env.map
        this.bearing = 0
        this.pitch = 60
        map.setBearing(this.bearing)
        map.setPitch(this.pitch)
    }

    easeTo(t) {
        return t * (2 - t);
    }

    move(pos, bearing, pitch) {
        let that = this
        if (pitch) {
            env.map.easeTo({
                pitch: pos,
                easing: that.easeTo
            })
        } else {
            if (bearing) {
                env.map.easeTo({
                    bearing: pos,
                    easing: that.easeTo
                })
            } else {
                env.map.panBy(pos, {
                    easing: that.easeTo
                })
            }
        }

    }

    clearSelect() {
        let that = this
        this.timeout = setTimeout(function () {
            that.updateButtonState()
            clearTimeout(that.timeout)
        }, 1000 * 1)
    }

    updateButtonState(item) {
        let state = {
            'move-forward': false,
            'move-backward': false,
            'move-left': false,
            'move-right': false,
            'rot-left': false,
            'rot-right': false,
            'pitch-up': false,
            'pitch-down': false
        }

        if (item) {
            state[item] = true
        }

        this.setState(state)
    }

    goDirection(e) {
        switch (e.target) {
            case this.refs["rot-left"]:
                this.move(env.map.getBearing() - 25, true);
                this.updateButtonState("rot-left")
                break;
            case this.refs["rot-right"]:
                this.move(env.map.getBearing() + 25, true);
                this.updateButtonState("rot-right")
                break;
            case this.refs["move-forward"]:
                this.move([0, -100]);
                this.updateButtonState("move-forward")
                break;
            case this.refs["move-backward"]:
                this.move([0, 100]);
                this.updateButtonState("move-backward")
                break;
            case this.refs["move-left"]:
                this.move([-100, 0]);
                this.updateButtonState("move-left")
                break;
            case this.refs["move-right"]:
                this.move([100, 0]);
                this.updateButtonState("move-right")
                break;
            case this.refs["pitch-up"]:
                this.move(env.map.getPitch() + 5, false, true);
                this.updateButtonState("pitch-up")
                break;
            case this.refs["pitch-down"]:
                if (env.map.getPitch() - 5 <= 0) {
                    return
                }
                this.updateButtonState("pitch-down")
                this.move(env.map.getPitch() - 5, false, true);
                break;
        }
        this.clearSelect()
    }

    registerKeydown() {
        document.onkeydown = false
        let that = this
        let map = env.map
        let speed = 0.08
        let chapters = {
            'a': {
                bearing: 70.5,
                center: [116.377252,39.986506],
                zoom: 14,
                pitch: 55,
                speed: speed,
                curve: 1
            },
            'b': {
                bearing: -50,
                duration: 6000,
                center: [116.353777, 40.02324],
                bearing: 150,
                zoom: 14.8,
                pitch: 30,
                speed: speed,
                curve: 1
            },
            'c': {
                bearing: -10,
                center: [116.499118, 40.000075],
                zoom: 14.2,
                pitch: 40,
                speed: speed,
                curve: 1
            },
            'd': {
                bearing: 90,
                center: [116.488197, 39.87509],
                zoom: 13.5,
                speed: speed,
                pitch: 60,
                curve: 1
            },
            'e': {
                bearing: 150,
                center: [116.439377, 39.870708],
                zoom: 15.3,
                pitch: 20,
                speed: speed,
                curve: 1
            },
            'f': {
                bearing: 180,
                center: [116.344345, 39.859543],
                zoom: 12.3,
                pitch: 60,
                speed: speed,
                curve: 1
            },
            'g': {
                bearing: 45,
                center: [116.310229, 39.922166],
                zoom: 14.3,
                pitch: 40,
                speed: speed,
                curve: 1
            },
            'h': {
                bearing: 0,
                center: [116.278151, 39.875484],
                zoom: 14.3,
                pitch: 20,
                speed: speed,
                curve: 1
            }
        }

        document.onkeydown = function (e) {
            let keynum
            let keychar

            if (window.event) {
                keynum = e.keyCode
            }
            else if (e.which) {
                keynum = e.which
            }

            keychar = String.fromCharCode(keynum)
            if (window.event.shiftKey) {
                let list = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'p']
                for (let i = 0; i < 9; i++) {
                    if (list[i] == keychar.toLowerCase()) {
                        map.flyTo(chapters[keychar.toLowerCase()])
                    }
                }
            }

            if (window.event.shiftKey && keychar.toLowerCase() == 'p') {
                if (that.props.map.init.fly) {
                    that.props.mapFlyAction(false)
                    that.props.mapFlyStepAction(6)
                } else {
                    that.props.mapFlyAction(true)
                }
            }

            switch (keynum) {
                case 38:
                    that.goDirection({target: that.refs["move-forward"]})
                    break;
                case 40:
                    that.goDirection({target: that.refs["move-backward"]})
                    break;
                case 37:
                    that.goDirection({target: that.refs["move-left"]})
                    break;
                case 39:
                    that.goDirection({target: that.refs["move-right"]})
                    break;
                case 33:
                case 189:
                    that.goDirection({target: that.refs["rot-left"]})
                    break;
                case 34:
                case 187:
                    that.goDirection({target: that.refs["rot-right"]})
                    break;
                case 219:
                    that.goDirection({target: that.refs["pitch-up"]})
                    break;
                case 221:
                    that.goDirection({target: that.refs["pitch-down"]})
                    break;
                case 188:
                    env.map.easeTo({
                        zoom: env.map.getZoom() - 0.5,
                        easing: that.easeTo
                    })
                    break;
                case 190:
                    env.map.easeTo({
                        zoom: env.map.getZoom() + 0.5,
                        easing: that.easeTo
                    })
                    break;
            }
        }
    }

    componentWillUnmount() {
        document.onkeydown = false
    }

    render() {
        let that = this;
        this.registerKeydown()

        const { map,mapFlyAction} = this.props

        const divInfo = <div className="map-control-btn-group">
            <span className="glyphicon glyphicon-plus"
                  onClick={this.btnZoomInClick}></span>
            <span className="glyphicon glyphicon-minus"
                  onClick={this.btnZoomOutClick}></span>
            <span className="glyphicon glyphicon-share-alt"
                  onClick={this.btnBearingClick}></span>
                <span className="glyphicon glyphicon-arrow-up"
                      onClick={this.btnPitchClick}></span>
        </div>

        return <div>
            <div className="map-control-container" id="mapControl">
                <div className="map-control-btn-group">
                    <img width="26" height="26" src={icon3d} ref="icon3dImg"
                         onClick={this.btnPitchClick}/>
                </div>
            </div>
            <div className="map-control-fly-ctrl-container">
                <div>
                    <span ref="rot-left"
                          className={this.state['rot-left']?"map-control-fly-btn radius-1 keydown":"map-control-fly-btn radius-1"}
                          onClick={this.goDirection}>↺</span>
                    <span ref="move-forward"
                          className={this.state['move-forward']?"map-control-fly-btn keydown":"map-control-fly-btn"}
                          onClick={this.goDirection}>↑</span>
                    <span ref="rot-right"
                          className={this.state['rot-right']?"map-control-fly-btn radius-2 keydown":"map-control-fly-btn radius-2"}
                          onClick={this.goDirection}>↻</span>
                </div>
                <div>
                    <span ref="move-left"
                          className={this.state['move-left']?"map-control-fly-btn radius-4 keydown":"map-control-fly-btn radius-4"}
                          onClick={this.goDirection}>←</span>
                    <span ref="move-backward"
                          className={this.state['move-backward']?"map-control-fly-btn keydown":"map-control-fly-btn"}
                          onClick={this.goDirection}>↓</span>
                    <span ref="move-right"
                          className={this.state['move-right']?"map-control-fly-btn radius-3 keydown":"map-control-fly-btn radius-3"}
                          onClick={this.goDirection}>→</span>
                </div>
            </div>
            <div className="map-control-pitch-ctrl-container">
                <div>
                    <span ref="pitch-up"
                          className={this.state['pitch-up']?'map-control-fly-btn radius-1 radius-2 keydown':'map-control-fly-btn radius-1 radius-2'}
                          onClick={this.goDirection}>↑</span>
                </div>
                <div>
                    <span ref="pitch-down"
                          className={this.state['pitch-down']?'map-control-fly-btn radius-3 radius-4 keydown':'map-control-fly-btn radius-3 radius-4'}
                          onClick={this.goDirection}>↓</span>
                </div>
            </div>
        </div>

    }
}

MapControl.propTypes = {}