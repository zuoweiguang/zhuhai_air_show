import React, {Component, PropTypes} from "react"
import env from "../../core/env"


export default class CityLink extends Component {
    constructor(props, context){
        super(props, context)
        this.jumpToCity = this.jumpToCity.bind(this)
    }

    jumpToCity(){
        const cityCoord = this.props.cityCoord
        const center = [Number.parseFloat(cityCoord.x),Number.parseFloat(cityCoord.y)]
        if(env.map){
            env.map.setCenter(center)
        }
    }

    render() {
        const { cityName,cityCoord} = this.props
        return <a className="cityList_a" key={cityName} href="javascript:void(0);" onClick={this.jumpToCity}>{cityName}</a>
    }
}

CityLink.propTypes = {
    cityName: PropTypes.string.isRequired,
    cityCoord: PropTypes.object.isRequired
}


export default class CityListPanel extends Component {
    constructor(props, context) {
        super(props, context)
        this.openCityPanel = this.openCityPanel.bind(this)
        this.closeCityPanel = this.closeCityPanel.bind(this)
        this.getCityListContent = this.getCityListContent.bind(this)
    }

    componentDidMount() {
        this.props.mapControllCityInfoListAction()
        this.props.mapControllCityPanelVisibleAction(false)
    }

    openCityPanel(e) {
        e.preventDefault()
        this.props.mapControllCityPanelVisibleAction(true)
    }

    closeCityPanel(e) {
        e.preventDefault()
        this.props.mapControllCityPanelVisibleAction(false)
    }

    getCityListContent(cityInfoList) {
        let cityPanel = []
        for (let provIndex in cityInfoList) {
            let provTitleDT = <dt className="cityList_dt">{provIndex}</dt>
            let cityInfos = cityInfoList[provIndex]
            let cityLinks = []
            if (cityInfos) {
                for (let cityIndex in cityInfos) {
                    let city = cityInfos[cityIndex]
                    let city_link = <CityLink key = {cityIndex} cityName={cityIndex} cityCoord={city} />
                    cityLinks.push(city_link)

                }
            }
            let provTitleDD = <dd className="cityList_dd">{cityLinks}</dd>
            let provBox = <dl className="dl-horizontal cityList_dl">{provTitleDT}{provTitleDD}</dl>
            cityPanel.push(provBox)
        }
        return cityPanel
    }

    render(){
        const {solution, map, actions, mapControllCityInfoListAction, mapControllCityPanelVisibleAction} = this.props
        const cityListContent = this.getCityListContent(map.controll.cityInfoList)

        return <div className="cityList-modal" style={{display: map.controll.cityPanelVisible ? 'block' : 'none'}}>
            <div className="cityList-dialog">
                <div className="modal-content cityList-content">
                    <div className="modal-header cityList-header">
                        <button type="button" className="close" onClick={this.closeCityPanel}>
                            <span aria-hidden="true">&times;</span>
                            <span className="sr-only">Close</span>
                        </button>
                        <h4 className="cityList-title">请选择城市</h4>
                    </div>

                    <div className="modal-body cityList_body">
                        <div className="city-list">
                            {cityListContent}
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-default" onClick={this.closeCityPanel}>关闭</button>
                    </div>
                </div>
            </div>
        </div>
    }
}



CityListPanel.propTypes = {
    solution: PropTypes.object.isRequired,
    map: PropTypes.object.isRequired
}




