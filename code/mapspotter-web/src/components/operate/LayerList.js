import React, { Component, PropTypes } from 'react'
import LayerItem from './LayerItem'

export default class LayerList extends Component {
    constructor(props, context) {
        super(props, context)
        this.reorderLayerList = this.reorderLayerList.bind(this)
        this.handleDrop = this.handleDrop.bind(this)
        this.handleDragOver = this.handleDragOver.bind(this)
        this.layerMove = this.layerMove.bind(this)
        this.layerItemHeight = 32
        this.layerItemWidth = 200
    }

    reorderLayerList(layers) {
        return layers.sort((a, b)=> {
            return b.zindex - a.zindex
        })
    }

    /*
     * 1 always makes up
     * 0 always makes down
     * */
    layerMove(domId, moveCount, upOrDown = 1) {
        let id = parseInt(domId.split("-")[3])
        let layers = []
        for (let i = this.props.solution.layers.length - 1; i > -1; i--) {
            layers.push(this.props.solution.layers[i])
        }
        layers.reverse()

        let layer = layers[id]
        let zindex = layer.zindex

        let datas = []
        if (upOrDown) {
            datas.push({id: layers[id].id, zindex: (zindex + moveCount)})
            for (let i = id - moveCount; i < id; i++) {
                let aLayer = layers[i]
                if (aLayer.type == "animation") {
                    return
                }
                datas.push({id: aLayer.id, zindex: aLayer.zindex - 1})
            }
        } else {
            datas.push({id: layers[id].id, zindex: (zindex - moveCount)})
            for (let i = id + 1; i < id + moveCount + 1; i++) {
                let aLayer = layers[i]
                if (aLayer.type == "animation") {
                    return
                }
                datas.push({id: aLayer.id, zindex: aLayer.zindex + 1})
            }
        }
        this.props.actions.solutionLayerEditZIndexAction(datas)
    }

    handleDrop(e) {
        e.preventDefault();
        let id = e.dataTransfer.getData("id")
        let screenX = parseInt(e.dataTransfer.getData("screenX"))
        let screenY = parseInt(e.dataTransfer.getData("screenY"))
        let scrollTop = parseInt(e.dataTransfer.getData("scrollTop"))

        let offsetX = e.screenX - screenX
        let offsetY = (e.screenY + document.getElementById("layer-container-only-id").scrollTop) - (screenY + scrollTop)

        if (Math.abs(offsetX) < this.layerItemWidth && Math.abs(offsetY) > this.layerItemHeight / 2) {
            let moveCount = Math.abs(Math.round(offsetY / this.layerItemHeight))
            let upOrDown = offsetY < 0 ? 1 : 0
            this.layerMove(id, moveCount, upOrDown)
        }

        e.dataTransfer.clearData("id");
        e.dataTransfer.clearData("screenX");
        e.dataTransfer.clearData("screenY");
        e.dataTransfer.clearData("scrollTop");
    }

    handleDragOver(e) {
        e.preventDefault();
    }

    render() {
        const { solution,solutionId,layers,actions,menuToggleAction,panelDisplayInfoAction,mapActions} = this.props
        let reverseLayers = this.reorderLayerList(layers)
        const layerItems = reverseLayers.map((layer, i) => {
            return <LayerItem key={layer.id} layerItemId={"order-layers-id-"+i} {...mapActions} solution={solution}
                              solutionId={solutionId} layer={layer} {...actions} menuToggleAction={menuToggleAction}
                              panelDisplayInfoAction={panelDisplayInfoAction}/>
        })
        return <div id="layer-container-only-id" onDrop={this.handleDrop} onDragOver={this.handleDragOver}
                    className="operate-layer-list">
            {layerItems}
        </div>
    }
}

LayerList.propTypes = {
    layers: PropTypes.array.isRequired,
    actions: PropTypes.object.isRequired
}