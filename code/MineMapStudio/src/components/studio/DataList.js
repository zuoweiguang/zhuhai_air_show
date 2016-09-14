import React, { Component, PropTypes } from 'react'
import UserTool from '../../utils/UserTool'

export default class DataList extends Component {
    constructor(props, context) {
        super(props, context)
    }

    componentDidMount() {
        this.props.studioActions.studioMenuLinkAction('data')
    }

    render() {
        const {studioActions} = this.props
        return <div>
            数据建设中...
        </div>
    }
}

DataList.propTypes = {
    studioActions: PropTypes.object.isRequired
}