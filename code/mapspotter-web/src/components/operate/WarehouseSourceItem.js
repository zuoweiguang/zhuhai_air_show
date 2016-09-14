import React, { Component, PropTypes } from 'react'
import {Media} from 'react-bootstrap'

export default class WarehouseSourceItem extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleChange = this.handleChange.bind(this)
    }

    componentDidMount() {
        this.props.actions.warehouseSourceLayersAction(this.props.warehouseSourceItem.id)
    }

    handleChange(e) {
        this.props.actions.warehouseSourceLayerChangeAction(e.target.value)
    }

    render() {
        const {warehouseSourceItem,actions} = this.props
        let header = warehouseSourceItem.desc;
        const warehouseCheckboxItem = warehouseSourceItem["source-layers"] ? warehouseSourceItem["source-layers"].map((layer) => {
            return <div className="item-checkbox" key={layer.id}>
                <div className="checkbox">
                    <label>
                        <input value={layer.id} type="checkbox" checked={layer.checked}
                               onChange={this.handleChange}/> {layer.name}
                    </label>
                </div>
            </div>
        }) : null
        return <div className="item-container clearfix">
            <div className="item-header">{header}</div>
            <div className="item-body clearfix">
                {warehouseCheckboxItem}
            </div>
        </div>
    }

}

WarehouseSourceItem.propTypes = {
    warehouseSourceItem: PropTypes.object.isRequired,
    actions: PropTypes.object.isRequired
}

