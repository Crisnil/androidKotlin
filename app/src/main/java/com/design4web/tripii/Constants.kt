package com.design4web.tripii

class Constants {

    interface ACTION {
        companion object {
            val MAIN_ACTION = "com.aprosoftech.trippr.action.main"
            val PREV_ACTION = "com.aprosoftech.trippr.action.prev"
            val PLAY_ACTION = "com.aprosoftech.trippr.action.play"
            val NEXT_ACTION = "com.aprosoftech.trippr.action.next"
            val STARTFOREGROUND_ACTION = "com.aprosoftech.trippr.action.startforeground"
            val STOPFOREGROUND_ACTION = "com.aprosoftech.trippr.action.stopforeground"
        }
    }

    interface NOTIFICATION_ID {
        companion object {
            val FOREGROUND_SERVICE = 101
        }
    }
}
