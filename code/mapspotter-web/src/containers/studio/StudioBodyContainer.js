import React, { Component,PropTypes } from 'react'
import '../../scss/studio/studioBody.scss'
import SolutionMenuContainer from './SolutionMenuContainer'

export default class StudioBodyContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const bodyHeight = document.body.clientHeight - 80;
        const bodyStyle = {
            height: bodyHeight + 'px'
        }
        const { contentView } = this.props
        return (
            <div className="page-body-container" style={bodyStyle}>
                <SolutionMenuContainer/>
                <div className="solution-body-box">
                    {contentView}
                </div>
            </div>
        )
    }
}
