/* Copyright (c) 2021 BlackBerry Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.msohm.blackberry.samples.presencedemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.good.gd.GDAndroid;
import com.good.gd.GDAppServer;
import com.good.gd.GDServiceDetail;
import com.good.gd.GDServiceProvider;
import com.good.gd.GDServiceType;
import com.good.gd.GDStateListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements GDStateListener
{

    //The service IDs.
    private static final String PRESENCE_SERVICE = "com.good.gdservice.enterprise.presence";

    //Keys used for SharedPreferences.
    public static final String SECURE_STORE_SHARED_PREFS = "bems.pres.sp";
    public static final String DEVICE_ID_UUID_KEY = "device.id.uuid.key";

    //ArrayLists to hold the server details.
    ArrayList<BemsServer> presenceServers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Initialize Good Dynamics.
        GDAndroid.getInstance().activityInit(this);

        setContentView(R.layout.activity_main);
    }

    //Looks for a service provider that supports the docs service.
    public void getServiceProviders(View view)
    {
        Vector<GDServiceProvider> providers = GDAndroid.getInstance().getServiceProvidersFor(
                PRESENCE_SERVICE,
                "1.0.0.0", GDServiceType.GD_SERVICE_TYPE_SERVER);

        String serviceDetails = parseServiceDetails(providers);

        TextView output = (TextView)findViewById(R.id.outputTextView);
        output.setText(serviceDetails);

    }


    //Extract the service and server details.
    private String parseServiceDetails(Vector<GDServiceProvider> providers)
    {
        //Creates the string to display on screen.
        StringBuffer sb = new StringBuffer();

        //Holds the server details as we verify the services the server supports.
        ArrayList<BemsServer> serverCache = new ArrayList<>();

        for (int count = 0; count < providers.size(); count++)
        {
            GDServiceProvider provider = providers.get(count);
            sb.append("Service: ");
            sb.append(count);
            sb.append('\n');
            sb.append("Application ID: ");
            sb.append(provider.getIdentifier());
            sb.append('\n');
            sb.append("Version: ");
            sb.append(provider.getVersion());
            sb.append('\n');
            sb.append("Name: ");
            sb.append(provider.getName());
            sb.append('\n');
            sb.append("Address: ");
            sb.append(provider.getAddress());
            sb.append('\n');
            sb.append("Servers:");
            sb.append('\n');

            Vector<GDAppServer> servers = provider.getServerCluster();
            serverCache.clear();

            for (int serverCount = 0; serverCount < servers.size(); serverCount++)
            {
                GDAppServer server = servers.get(serverCount);

                BemsServer gserver = new BemsServer(server.server + ":" + server.port, server.priority);
                serverCache.add(gserver);

                sb.append("Server ");
                sb.append(serverCount);
                sb.append(' ');
                sb.append(server.server);
                sb.append(':');
                sb.append(server.port);
                sb.append(" Priority: ");
                sb.append(server.priority);
                sb.append('\n');
            }

            sb.append("Services:");
            sb.append('\n');

            Vector<GDServiceDetail> serviceDetails = provider.getServices();

            for (int serviceCount = 0; serviceCount < serviceDetails.size(); serviceCount++)
            {
                GDServiceDetail serviceDetail = serviceDetails.get(serviceCount);

                if (serviceDetail.getIdentifier().contains(PRESENCE_SERVICE))
                {
                    presenceServers = serverCache;
                }

                sb.append("Service ID: ");
                sb.append(serviceDetail.getIdentifier());
                sb.append('\n');
                sb.append("Service Version: ");
                sb.append(serviceDetail.getVersion());
                sb.append('\n');
                sb.append("Service Type: ");

                if (serviceDetail.getServiceType() == GDServiceType.GD_SERVICE_TYPE_APPLICATION)
                {
                    sb.append("Application");
                }
                else if (serviceDetail.getServiceType() == GDServiceType.GD_SERVICE_TYPE_SERVER)
                {
                    sb.append("Server");
                }
                sb.append('\n');
            }
        }

        if (sb.length() == 0)
        {
            sb.append("BEMS Services not found.");
        }

        return sb.toString();
    }


    public void onBrowsePresenceRich(View view)
    {
        //Ensure there is a known server to connect to.
        if (presenceServers.size() == 0)
        {
            TextView output = (TextView)findViewById(R.id.outputTextView);
            output.setText("Error! BEMS Server Unknown.  Execute get service providers first.  Press GO! button above.");
        }
        else
        {
            Intent docsIntent = new Intent(this, PresenceActivityRich.class);
            docsIntent.putExtra("com.msohm.blackberry.samples.bemsdemo.PresenceServers", presenceServers);
            startActivity(docsIntent);
        }
    }

    public void onBrowsePresenceRaw(View view)
    {
        //Ensure there is a known server to connect to.
        if (presenceServers.size() == 0)
        {
            TextView output = (TextView)findViewById(R.id.outputTextView);
            output.setText("Error! BEMS Server Unknown.  Execute get service providers first.  Press GO! button above.");
        }
        else
        {
            Intent presenceIntent = new Intent(this, PresenceActivityRaw.class);
            presenceIntent.putExtra("com.msohm.blackberry.samples.bemsdemo.PresenceServers", presenceServers);
            startActivity(presenceIntent);
        }
    }

    @Override
    public void onAuthorized() {
        //Create a UUID and store in GD Shared Preferences.

        SharedPreferences sp = GDAndroid.getInstance().getGDSharedPreferences(SECURE_STORE_SHARED_PREFS,
                android.content.Context.MODE_PRIVATE);

        if (!sp.contains(DEVICE_ID_UUID_KEY))
        {
            sp.edit().putString(SECURE_STORE_SHARED_PREFS, UUID.randomUUID().toString());
        }
    }

    @Override
    public void onLocked() {  }

    @Override
    public void onWiped() {  }

    @Override
    public void onUpdateConfig(Map<String, Object> map) {  }

    @Override
    public void onUpdatePolicy(Map<String, Object> map) {  }

    @Override
    public void onUpdateServices() {  }

    @Override
    public void onUpdateEntitlements() {  }
}
