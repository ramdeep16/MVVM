package com.score.interview.activity

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.score.interview.data.Team
import com.score.interview.helperClasses.SortOptions
import com.score.interview.network.TeamsAPIClass
import io.reactivex.disposables.CompositeDisposable

class TeamListViewModel : ViewModel() {

    private val mCompositeDisposable = CompositeDisposable()
    private val teamsAPIClass: TeamsAPIClass = TeamsAPIClass()

    private lateinit var progressBarLiveData: MutableLiveData<Boolean>
    private lateinit var messageLiveData: MutableLiveData<String>
    private lateinit var teamsListLiveData: MutableLiveData<List<Team>>
    private lateinit var teamLiveData: MutableLiveData<Team>

    /**
     * Sort option Enum
     * Will be set according to menu click
     */
    var sortingOptionSelected: SortOptions = SortOptions.ALPHABETICAL
        set(value) {
            field = value
            if (teamsListLiveData.value != null) {
                sortList(teamsListLiveData.value!!)
            }
        }

    fun getProgressBarLiveData(): MutableLiveData<Boolean> {

        progressBarLiveData = MutableLiveData()
        progressBarLiveData.value = false
        return progressBarLiveData
    }

    fun getMessageLiveData(): MutableLiveData<String> {

        messageLiveData = MutableLiveData()
        return messageLiveData
    }

    fun getTeamList(): MutableLiveData<List<Team>> {

        teamsListLiveData = MutableLiveData()
        fetchTeams()
        return teamsListLiveData
    }

    /**
     * Fetching team from network and database
     */
    private fun fetchTeams() {
        progressBarLiveData.value = true
        mCompositeDisposable.add(
            teamsAPIClass.getTeams()
                .subscribe({ teams ->
                    if (teams != null) {
                        progressBarLiveData.value = false
                        sortList(teams)
                    }
                }, {
                    handleError(throwable = it)
                })
        )
    }

    /**
     * SORTING LIST
     */
    private fun sortList(teamsList: List<Team>) {

        teamsListLiveData.value = when (sortingOptionSelected) {
            SortOptions.ALPHABETICAL -> teamsList.sortedBy { it.fullName }
            SortOptions.WINS_DESCENDING -> teamsList.sortedByDescending { it.wins }
            SortOptions.WINS_ASCENDING -> teamsList.sortedBy { it.wins }
            SortOptions.LOSSES_ASCENDING -> teamsList.sortedBy { it.losses }
            SortOptions.LOSSES_DESCENDING -> teamsList.sortedByDescending { it.losses }
        }
    }

    private fun handleError(throwable: Throwable) {
        messageLiveData.value = throwable.localizedMessage
    }

    /*
     * Below Code is For Team Detail Activity
     * */
    fun getTeamDetail(teamID: Int): MutableLiveData<Team> {

        teamLiveData = MutableLiveData()
        getTeamWithID(teamID)
        return teamLiveData
    }

    //Team Detail From database
    private fun getTeamWithID(teamID: Int) {

        progressBarLiveData.value = true
        mCompositeDisposable.add(
            teamsAPIClass.getTeamsWithID(teamID)
                .subscribe({ team ->
                    if (team != null) {
                        progressBarLiveData.value = false
                        teamLiveData.value = team
                    }
                }, {
                    handleError(throwable = it)
                })
        )
    }
}