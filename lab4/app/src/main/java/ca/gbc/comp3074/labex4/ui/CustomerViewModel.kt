package ca.gbc.comp3074.labex4.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.labex4.data.Customer
import ca.gbc.comp3074.labex4.data.CustomerDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val customerDao: CustomerDao
) : ViewModel() {

    val customers: Flow<List<Customer>> = customerDao.getAllCustomers()

    // viewModelScope cancels these if the ViewModel is cleared, so nothing keeps running after the screen is gone
    fun addCustomer(name: String, age: Int, isActive: Boolean) {
        viewModelScope.launch {
            customerDao.insert(Customer(name = name, age = age, isActive = isActive))
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            customerDao.delete(customer)
        }
    }
}
