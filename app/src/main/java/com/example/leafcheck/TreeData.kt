package com.example.leafcheck

import java.util.Date

data class TreeData(
    var treeName: String? = null,
    var treeType: Int? = 0,
    var treeCond: String? = null,
    var treeDesc: String? = null,
    var condDate: Date = Date()) {
    // Constructor without parameters
    constructor() : this("", 0, "", "", Date())
}
