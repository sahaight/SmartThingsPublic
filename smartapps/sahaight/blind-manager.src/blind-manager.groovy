/**
 *  Window Shade Management
 *
 *  Copyright 2016 Stewart Haight
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  I wrote this app to control shades and haven't had an opportunity to test it with blinds.
 */
definition(
    name: "Blind Manager",
    namespace: "sahaight",
    author: "Stewart Haight",
    description: "Window Shade Control",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Shade(s) Setup" ) {
        input("shadeList", "capability.windowShade", title: "Pick your shades to control:", multiple: true)
//        input ("openingTime", "number", title: "Average time to open shade? (seconds)", 
//           default: "15", required: true, displayDuringSetup: true )
	}
    section("Open Shade(s) fully when...") {
   	input("openFixedTime", "time", title: "Specific Time to Open Shade", required: false, displayDuringSetup: true, multiple: true)
        input("openSunriseTime", "number", title: "Minutes after sunrise (0=not used)", required: false, displayDuringSetup: true)
        input("openSunsetTime", "number", title: "Minutes before sunset (0=not used)", required: false, displayDuringSetup: true)
//       	input("lightSensor", "capability.illuminanceMeasurement", title: "Light Sensor", hideWhenEmpty: true, required: false, displayDuringSetup: true)
//    	input("lightLevel", "number", title: "Lux Level")   */	
//          input("openLight", "capability.switch", title: "Light(s) to turn off when opening", required: false, displayDuringSetup: true, multiple: true)
            }
    section("Open Shade(s) part-way when...") {
    	input("partialTime", "time", title: "Specific Time:", required: false, displayDuringSetup: true, multiple: true)
        input("partialSunriseTime", "number", title: "Minutes after sunrise (0=not used)", required: false, displayDuringSetup: true)
        input("partialSunsetTime", "number", title: "Minutes before sunset (0=not used)", required: false, displayDuringSetup: true)
	}    
    section("Close Shade(s) when...") {
    	input("closeTime", "time", title: "Specific Time to Open Shade", required: false, displayDuringSetup: true, multiple: true)
        input("closeSunriseTime", "number", title: "Minutes after sunrise (0=not used; negative=before sunrise)", required: false, displayDuringSetup: true)
        input("closeSunsetTime", "number", title: "Minutes before sunset (0=not used; negative=after sunset)", required: false, displayDuringSetup: true)
//		input("closeLight", "capability.switch", title: "Light to turn on when closing", required: false, displayDuringSetup: true, multiple: true)
	}    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    log.debug "initialize 1"
//    subscribe(shadeList, "shades", shadeHandler)
    log.debug "initialize 2"
    subscribe(location, "sunriseTime", sunriseTimeHandler)
    log.debug "initialize 3"
    subscribe(location, "sunsetTime", sunsetTimeHandler)
    log.debug "initialize 4"
    //schedule it to run today too
    scheduleTurnOn(location.currentValue("sunsetTime"))
    log.debug "initialize 5"
    //schedule fixed times
    if (openTime) { schedule(openTime, openShades) }
    log.debug "initialize 6"
    if (closeTime) { schedule(closeTime, closeShades) }
    if (partialTime) { schedule(partialTime, partialShades) }
}

def sunriseTimeHandler(evt) {
    //when I find out the sunset time, schedule the shades to open with an offset
    scheduleSunriseEvents(evt.value)
}
def sunsetTimeHandler(evt) {
    //when I find out the sunset time, schedule the lights to turn on with an offset
    scheduleSunsetEvents(evt.value)
}

def scheduleSunriseEvents(sunriseString) {
	def sunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
	//calculate the offset
     //sunrise relative times
    if (openSunriseTime && opentSunriseTime!=0) {
    	def openRiseTime = new Date(sunriseTime.time - (openSunriseTime * 60 * 1000))
		runOnce(openRiseTime, openShades)
	}
    if (closeSunriseTime) {
    	def closeRiseTime = new Date(sunriseTime.time - (closeSunriseTime * 60 * 1000))
		runOnce(closeRiseTime, closeShades) 
	}
    if (partialSunriseTime) { 
    	def partialRiseTime = new Date(sunriseTime.time - (partialSunriseTime * 60 * 1000))
		runOnce(partialSunriseTime, partialShades)
	}
}
def scheduleSunsetEvents(sunsetString) {
	def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)
	//calculate the offset
    //sunset relative times
    if (openSunsetTime && openSunsetTime != 0) {
    	def openSetTime = new Date(sunsetTime.time - (openSunsetTime * 60 * 1000))
		runOnce(openSetTime, openShades)
	}
    if (closeSunsetTime) {
    	def closeSetTime = new Date(sunsetTime.time - (closeSunsetTime * 60 * 1000))
		runOnce(closeSetTime, closeShades) 
	}
    if (partialSunsetTime) { 
    	def partialSetTime = new Date(sunsetTime.time - (partialSunsetTime * 60 * 1000))
		runOnce(partialSunsetTime, partialShades)
	}
}
    
def openShades() {
	sendEvent (name:"shades", value: "open")
}

def closeShades() {
	sendEvent(name:"shades", value: "close")
/*    if(closeLight) { */
}

def partialShades() {
	sendEvent(name:"shades", value: "partially open")
}


