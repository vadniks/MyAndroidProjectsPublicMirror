package com.some.hw3a3.processing.data.rest

import com.some.hw3a3.processing.data.EndPoints
import com.some.hw3a3.processing.model.GithubUser
import com.some.hw3a3.processing.model.Repo
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * @author Vad Nik.
 * @version dated Sep 14, 2018.
 * @link http://github.com/vadniks
 */
object NetApiClient {
    private val nac = ServiceGenerator().createService(EndPoints::class.java)

    fun getUser(user: String): Observable<GithubUser> = nac
            .getUser(user)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())

    @Deprecated("")
    private fun a(i: String, ar: ArrayList<GithubUser>) {
        getUser(i).subscribe(object : Observer<GithubUser> {

            override fun onComplete() {}

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: GithubUser) {
                println("testo mee $t")
                ar.add(t)
            }

            override fun onError(e: Throwable): Unit = e.printStackTrace()
        })
    }

    @Deprecated("")
    fun getUsers(users: Array<String>): Observable<GithubUser> {
        val ar = ArrayList<GithubUser>()
        for (i in users)
            a(i, ar)

        println("testo emt ${ar.isEmpty()}")

        val obs = Observable
                .range(0, ar.size)
                .map { i -> ar[i] }

        obs.subscribe { b -> println("testo empty $b") }

        return obs
    }

    fun getRepos(user: String): Flowable<ArrayList<Repo>> = nac
            .getRepos(user)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
}
