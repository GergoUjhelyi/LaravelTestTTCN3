<?php

namespace App\Http\Controllers;

use App\Models\UsageStat;
use Illuminate\Http\Request;

class UsageStatController extends Controller
{
    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        return response()->json(UsageStat::all(), 200);
    }

    /**
     * Show the form for creating a new resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function create()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     *
     * @param  \App\Http\Request  $request
     * @return \Illuminate\Http\Response
     */
    public function store(Request $request)
    {
        $usage = new UsageStat;

        $usage->plugin_id = $request->input('plugin_id');
        $usage->plugin_version_qualifier = $request->input('plugin_version_qualifier');
        $usage->plugin_version = $request->input('plugin_version');
        $usage->os_version = $request->input('os_version');
        $usage->os_arch = $request->input('os_arch');
        $usage->eclipse_version = $request->input('eclipse_version');
        $usage->eclipse_version_qualifier = $request->input('eclipse_version_qualifier');
        $usage->user_id = $request->input('user_id');
        $usage->java_version = $request->input('java_version');
        $usage->os_name = $request->input('os_name');
        $usage->info = $request->input('info');

        $usage->save();

        return $usage;
    }

    /**
     * Display the specified resource.
     *
     * @param  \App\Models\UsageStat  $usage_stat
     * @return \Illuminate\Http\Response
     */
    public function show($id)
    {
        return response()->json(UsageStat::findOrFail($id), 200);
    }

    /**
     * Show the form for editing the specified resource.
     *
     * @param  \App\Models\UsageStat  $usage_Stat
     * @return \Illuminate\Http\Response
     */
    public function edit(UsageStat $usage_Stat)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     *
     * @param  \App\Http\Requests\UpdateUsage_StatRequest  $request
     * @param  \App\Models\UsageStat  $usage_Stat
     * @return \Illuminate\Http\Response
     */
    public function update(UpdateUsage_StatRequest $request, UsageStat $usage_Stat)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     *
     * @param  \App\Models\UsageStat  $usage_Stat
     * @return \Illuminate\Http\Response
     */
    public function destroy(UsageStat $usage_Stat)
    {
        //
    }
}
