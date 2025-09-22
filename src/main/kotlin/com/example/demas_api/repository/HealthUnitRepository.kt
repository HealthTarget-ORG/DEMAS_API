package com.example.demas_api.repository

import com.example.demas_api.dto.MedicineTotalStock
import com.example.demas_api.model.HealthUnit
import com.example.demas_api.model.MedicineStock
import com.example.demas_api.model.enumeration.LocationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface HealthUnitRepository: MongoRepository<HealthUnit, String> {
    @Query("{ \$or: [ { 'name': { \$regex: ?0, \$options: 'i' } } ] }")
    fun findBySearchTerm(searchTerm: String, pageable: Pageable): Page<HealthUnit>

    @Query("{ \$and: [ " +
            "{ \$or: [ { 'name': { \$regex: ?0, \$options: 'i' } }, { 'city': { \$regex: ?0, \$options: 'i' } } ] }, " +
            "{ 'location_type': ?1 } " +
            "] }")
    fun findBySearchTermAndLocationType(searchTerm: String, locationType: LocationType, pageable: Pageable): Page<HealthUnit>

    @Aggregation(pipeline = [
        "{ \$unwind: '\$medicines' }",
        "{ \$match: { 'medicines.description': { \$regex: ?0, \$options: 'i' } } }",
        "{ \$group: { _id: '\$medicines.catmat_code', totalStock: { \$sum: '\$medicines.total_quantity' }, description: { \$first: '\$medicines.description' } } }",
        "{ \$match: { 'totalStock': { \$gt: 0 } } }",
        "{ \$sort: { description: 1 } }",
        "{ \$project: { _id: 0, description: 1, totalStock: 1 } }"
    ])
    fun aggregateStockAvailable(searchTerm: String, pageable: Pageable): List<MedicineTotalStock>

    @Aggregation(pipeline = [
        "{ \$unwind: '\$medicines' }",
        "{ \$match: { 'medicines.description': { \$regex: ?0, \$options: 'i' } } }",
        "{ \$group: { _id: '\$medicines.catmat_code', totalStock: { \$sum: '\$medicines.total_quantity' }, description: { \$first: '\$medicines.description' } } }",
        "{ \$match: { 'totalStock': { \$eq: 0 } } }",
        "{ \$sort: { description: 1 } }",
        "{ \$project: { _id: 0, description: 1, totalStock: 1 } }"
    ])
    fun aggregateStockUnavailable(searchTerm: String, pageable: Pageable): List<MedicineTotalStock>

    @Aggregation(pipeline = [
        "{ \$unwind: '\$medicines' }",
        "{ \$match: { 'medicines.description': { \$regex: ?0, \$options: 'i' } } }",
        "{ \$group: { _id: '\$medicines.catmat_code', totalStock: { \$sum: '\$medicines.total_quantity' } } }",
        "{ \$match: { 'totalStock': { \$gt: 0 } } }",
        "{ \$count: 'total' }"
    ])
    fun countStockAvailable(searchTerm: String): Long

    @Aggregation(pipeline = [
        "{ \$unwind: '\$medicines' }",
        "{ \$match: { 'medicines.description': { \$regex: ?0, \$options: 'i' } } }",
        "{ \$group: { _id: '\$medicines.catmat_code', totalStock: { \$sum: '\$medicines.total_quantity' } } }",
        "{ \$match: { 'totalStock': { \$eq: 0 } } }",
        "{ \$count: 'total' }"
    ])
    fun countStockUnavailable(searchTerm: String): Long

    @Aggregation(pipeline = [
        "{ \$unwind: '\$medicines' }",
        "{ \$match: { 'medicines.description': { \$regex: ?0, \$options: 'i' } } }",
        "{ \$group: { " +
                "_id: '\$medicines.catmat_code', " +
                "totalStock: { \$sum: '\$medicines.total_quantity' }, " +
                "description: { \$first: '\$medicines.description' } " +
                "} }",
        "{ \$sort: { description: 1 } }",
        "{ \$project: { _id: 0, description: 1, totalStock: 1 } }"
    ])
    fun aggregateStockAll(searchTerm: String, pageable: Pageable): List<MedicineTotalStock>

    @Aggregation(pipeline = [
        "{ \$unwind: '\$medicines' }",
        "{ \$match: { 'medicines.description': { \$regex: ?0, \$options: 'i' } } }",
        "{ \$group: { _id: '\$medicines.catmat_code' } }",
        "{ \$count: 'total' }"
    ])
    fun countStockAll(searchTerm: String): Long

    @Aggregation(pipeline = [
        "{ \$match: { 'medicines': { \$elemMatch: { 'description': { \$regex: ?0, \$options: 'i' }, 'total_quantity': { \$gt: 0 } } } } }",
        "{ \$addFields: { 'medicines': { " +
                "\$filter: { " +
                "input: '\$medicines', " +
                "as: 'med', " +
                "cond: { \$and: [ " +
                "{ \$regexMatch: { input: '\$\$med.description', regex: ?0, options: 'i' } }, " +
                "{ \$gt: [ '\$\$med.total_quantity', 0 ] } " +
                "] } " +
                "} " +
                "} } }",
    ])
    fun findUnitsWithMedicineInStock(searchTerm: String, pageable: Pageable): List<HealthUnit>

    @Aggregation(pipeline = [
        "{ \$match: { 'medicines': { \$elemMatch: { 'description': { \$regex: ?0, \$options: 'i' }, 'total_quantity': { \$gt: 0 } } } } }",
        "{ \$count: 'total' }"
    ])
    fun countUnitsWithMedicineInStock(searchTerm: String): Long

    fun findTopByOrderByLastStockUpdateDesc(): HealthUnit?

    @Aggregation(pipeline = [
        "{ \$match: { '_id': ?0 } }",
        "{ \$unwind: '\$medicines' }",
        "{ \$sort: { 'medicines.description': 1 } }",
        "{ \$replaceRoot: { newRoot: '\$medicines' } }"
    ])
    fun findAndPaginateMedicinesByUnit(cnesCode: String, pageable: Pageable): List<MedicineStock>

    @Aggregation(pipeline = [
        "{ \$match: { '_id': ?0 } }",
        "{ \$unwind: '\$medicines' }",
        "{ \$count: 'total' }"
    ])
    fun countMedicinesByUnit(cnesCode: String): Long

}