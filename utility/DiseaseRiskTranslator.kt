package com.cnr.phr_android.dashboard.monitor.utility

import com.cnr.phr_android.data.user.UserDiseaseRisk

class DiseaseRiskTranslator {
    fun getDiseaseRiskFromString(riskString:String): UserDiseaseRisk {
        return if(riskString == "S" || riskString == "s"){
            UserDiseaseRisk.SAFE
        }
        else if(riskString == "R" || riskString == "r"){
            UserDiseaseRisk.RISK
        }
        else if(riskString == "D" || riskString == "d"){
            UserDiseaseRisk.DANGER
        }
        else
            UserDiseaseRisk.SAFE
    }
}