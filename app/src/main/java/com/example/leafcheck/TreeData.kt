package com.example.leafcheck

import java.util.Date

data class TreeData(
    var treeId: String? = null,  // Add treeId field
    var treeName: String? = null,
    var treeType: Int? = 0,
    var treeCond: String? = null,
    var treeDesc: String? = null,
    var condDate: Date
) {
    constructor() : this("", "", 0, "", "", Date())
}
