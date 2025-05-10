package com.vn.uit.repository

import android.content.Context
import com.vn.uit.model.PestInfo

class PestInfoRepository(val context: Context) {

    fun getPestInfo(pestName: String): PestInfo {
        // In a real implementation, this would fetch data from a local database or API
        // For now, we're returning generic info with the pest name
        return PestInfo(
            name = pestName,
            description = "This is $pestName, a common agricultural pest. " +
                    "It affects crops by feeding on plant tissues and may transmit diseases. " +
                    "Early detection and management are crucial for preventing crop damage.",
            treatmentOptions = "Treatment options for $pestName:\n" +
                    "1. Monitor pest population regularly\n" +
                    "2. Use appropriate biological controls\n" +
                    "3. Apply recommended pesticides if necessary\n" +
                    "4. Implement crop rotation and sanitation practices\n" +
                    "5. Consider resistant crop varieties"
        )
    }
}
